package com.example.lotterysystem.service.mq;


import cn.hutool.core.date.DateUtil;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.common.utils.MailUtil;
import com.example.lotterysystem.common.utils.RedisUtil;
import com.example.lotterysystem.common.utils.SMSUtil;
import com.example.lotterysystem.controller.param.DrawPrizeParam;
import com.example.lotterysystem.controller.param.ShowWinningRecordsParam;
import com.example.lotterysystem.dao.dataobject.ActivityPrizeDO;
import com.example.lotterysystem.dao.dataobject.WinningRecordDO;
import com.example.lotterysystem.dao.mapper.ActivityPrizeMapper;
import com.example.lotterysystem.service.DrawPrizeService;
import com.example.lotterysystem.service.activitystatus.ActivityStatusManager;
import com.example.lotterysystem.service.dto.ActivityStatusConvertDTO;
import com.example.lotterysystem.service.dto.WinningRecordDTO;
import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityPrizeTiersEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import com.example.lotterysystem.service.enums.ActivityUserStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.lotterysystem.common.config.DirectRabbitConfig.QUEUE_NAME;

/**
 * 消费者类 MqReceiver
 */
@Slf4j
@Component
/**
 * @RabbitListener 注解：声明消息监听
 * @RabbitListener(queues = QUEUE_NAME)
 * 作用：标记一个方法为 RabbitMQ 消息的消费者，指定该方法监听哪个队列（queues 参数）。
 * 核心参数：
 * queues：指定监听的队列名称（如 QUEUE_NAME 为已定义的常量）。
 * queuesToDeclare：可同时声明并监听队列（适用于队列未提前创建的场景）。
 * exchange/routingKey：结合交换机和路由键监听（适用于 Direct/Topic 等交换机模式）。
 */
@RabbitListener(queues = QUEUE_NAME)
public class MqReceiver {
    private static final Logger logger = LoggerFactory.getLogger(MqReceiver.class);

    //通知模板代码
    private static final String WINNING_TEMPLATE_CODE = "SMS_465985911";

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private MailUtil mailUtil;

    @Autowired
    private SMSUtil smsUtil;

    @Autowired
    private ActivityStatusManager activityStatusManager;

    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;

    @Autowired
    private DrawPrizeService drawPrizeService;

    /**
     * 注入这个线程池对象
     */
    @Autowired
    private ThreadPoolTaskExecutor asyncServiceExecutor;

    /**
     * 抽奖请求消息的Mq处理,即抽奖逻辑的执行, key是string, value是DrawPrizeParam序列化后的字符串
     *
     * @param message
     * @RabbitHandler 注解：细化消息处理逻辑
     * 作用：在方法上标记该方法为消息处理的具体实现，通常用于多方法处理同一队列不同类型消息的场景
     * （需配合 @RabbitListener 使用）。
     * 应用场景：
     * 当一个队列可能接收不同类型的消息（如 JSON 格式的用户消息、订单消息），可通过 @RabbitHandler 让不同方法处理不同类型：
     */
    @RabbitHandler
    public void process(Map<String, Object> message) {
        logger.info("DirectReceiver消费者收到消息: " + message.toString());
        String msgData = (String) message.get("messageData");
        //从json格式的消息中解析出前端的抽奖消息请求
        DrawPrizeParam param = JacksonUtil.readValue(msgData, DrawPrizeParam.class);
        try {
            //1.核对抽奖信息的有效性
            drawPrizeService.checkDrawPrizeValid(param);
            //2.扭转活动状态
            convertStatus(param);
            //3.保存中奖结果
            List<WinningRecordDO> recordDOList = drawPrizeService.saveWinningRecords(param);
            //4.并发处理后续流程, execute执行
            syncExecute(recordDOList);
        } catch (ServiceException e) {
            logger.error("mq消息处理异常: {}", e.getCode(), e);
            //异常回滚中奖结果 + 活动/奖品状态, 保证事务一致性
            rollbackWinning(param);
            // 让此消息消费失败，⾃动转⼊死信队列中
            throw e;
        } catch (Exception e) {
            logger.error("mq消息处理异常:", e);
            //异常回滚中奖结果 + 活动/奖品状态, 保证事务一致性
            rollbackWinning(param);
            // 让此消息消费失败，⾃动转⼊死信队列中
            throw e;
        }
    }

    /**
     * 异常回滚中奖结果 + 活动/奖品状态, 保证事务一致性
     * 恢复处理请求之前的库表状态
     *
     * @param param
     */
    private void rollbackWinning(DrawPrizeParam param) {
        //1.判断活动/奖品/人员状态是否扭转成功
        if (!convertStatusSuccess(param)) {
            //直接返回, 人员状态如果没有扭转成功, 那么中奖记录也不会落库了
            //mq中, 状态转换在前面, 如果出现异常, 那么直接捕获, 就
            //没有后续操作的事了
            return;
        }
        //恢复活动/奖品/人员状态
        ActivityStatusConvertDTO statusConvertDTO = new ActivityStatusConvertDTO();
        statusConvertDTO.setActivityId(param.getActivityId());
        statusConvertDTO.setPrizeId(param.getPrizeId());
        statusConvertDTO.setUserIds(
                param.getWinnerList().stream()
                        .map(DrawPrizeParam.Winner::getUserId)
                        .toList()
        );
        //让活动状态管理者去处理状态回滚
        activityStatusManager.rollbackHandleEvent(statusConvertDTO);

        //2.判断中奖记录是否已经落库
        if (!hasRecords(param)) {
            //没有落库, 不用回滚了
            return;
        }

        //删除中奖记录和缓存
        List<Long> winnerIds = param.getWinnerList().stream()
                .map(DrawPrizeParam.Winner::getUserId)
                .toList();
        drawPrizeService.removeRecords(param.getActivityId(), param.getPrizeId(), winnerIds);

    }

    /**
     * 判断活动/奖品/人员状态是否扭转成功
     *
     * @param param
     * @return
     */
    private boolean convertStatusSuccess(DrawPrizeParam param) {
        // 1. ⾸先扭转状态⽅法⾃⾝保证了事务⼀致性，因此在这⾥我们只⽤判断活动/奖品/⼈员
        //其⼀是否扭转成功即可。
        // 2. 不⽤判断活动是否扭转成功，因为活动不⼀定要被扭转（依赖奖品）。
        // 结论：判断奖品是否抽完即可。
        //并且一次抽奖只抽一种奖品
        ActivityPrizeDO activityPrizeDO = activityPrizeMapper.selectByActivityAndPrizeId(
                param.getActivityId(), param.getPrizeId());
        //奖品是否已经被抽取, 被抽取, 由于事务的一致性,和原子性, 如果被抽取那么说明状态转换成功
        return activityPrizeDO.getStatus().equals(ActivityPrizeStatusEnum.COMPLETED.name());

    }

    /**
     * 判断中奖记录是否已经落库
     *
     * @param param
     * @return
     */
    private boolean hasRecords(DrawPrizeParam param) {
        ShowWinningRecordsParam showWinningRecordsParam = new ShowWinningRecordsParam();
        showWinningRecordsParam.setActivityId(param.getActivityId());
        showWinningRecordsParam.setPrizeId(param.getPrizeId());
        //通过前端的中奖消息请求来查询中奖的记录
        List<WinningRecordDTO> recordDTOList = drawPrizeService.
                showWinningRecords(showWinningRecordsParam);
        //为空, 说明还没有落库, 不为空, 说明已经落库
        return !CollectionUtils.isEmpty(recordDTOList);
    }

    /**
     * 使用策略和责任链模式
     * 策略模式（Strategy Pattern）:定义 AbstractActivityOperator 策列类，和其策略实现类
     * PrizeOperator、ActivityOperator 和 UserOperator。每个具体的操作类都实现了
     * AbstractActivityOperator 定义的接⼝，代表了不同的状态转换策略
     * 责任链模式（Chain of Responsibility Pattern）: 定义 ActivityStatusManager 接⼝类， 在
     * ActivityStatusManagerImpl 实现中，通过遍历operatorMap 中的所有操作符（策略），并按照
     * ⼀定的顺序执⾏，形成了⼀个责任链，每个操作符判断是否是⾃⼰的责任，如果是，则处理请
     * 求。
     * 责任链模式（Chain of Responsibility Pattern）是⼀种⾏为设计模式，它允许将⼀个请求沿着处理
     * 者对象组成的链进⾏传递。每个处理者对象都有责任去处理请求，或者将它传递给链中的下⼀个处理
     * 者。请求的传递⼀直进⾏，直到有⼀个处理者对象对请求进⾏了处理，或者直到链的末端仍未有处理
     * 者处理该请求
     *
     * @param param
     */
    private void convertStatus(DrawPrizeParam param) {
        //创建一个活动状态转换请求对象
        ActivityStatusConvertDTO statusConvertDTO = new ActivityStatusConvertDTO();

        //设置属性
        statusConvertDTO.setActivityId(param.getActivityId());
        //活动目标状态不一定会马上扭转到
        statusConvertDTO.setActivityTargetStatus(ActivityStatusEnum.COMPLETED);
        statusConvertDTO.setPrizeId(param.getPrizeId());
        statusConvertDTO.setPrizeTargetStatus(ActivityPrizeStatusEnum.COMPLETED);
        //中奖用户id列表通过前面的抽奖消息请求的中奖者列表通过stream流转换, 这个中奖这是个静态内部类
        statusConvertDTO.setUserIds(
                param.getWinnerList().stream()
                        .map(DrawPrizeParam.Winner::getUserId)
                        .distinct()
                        .toList()
        );
        statusConvertDTO.setUserTargetStatus(ActivityUserStatusEnum.COMPLETED);

        //让活动状态关管理接口真正去处理状态扭转
        activityStatusManager.handleEvent(statusConvertDTO);
    }

    /**
     * 并行通知中奖用户,处理抽奖后续流程
     *
     * @param recordDOList
     */
    public void syncExecute(List<WinningRecordDO> recordDOList) {
        //execute不会抛出异常影响主线程, 这里不用捕获异常
        //参数是具体的任务Runnable类型
        //抽奖的后续处理可以使用策略模式或者其他模式
        //策略模式: 通知处理父类, 有短信,邮件等通知处理方式
        //分别并行执行这两个通知任务
        //暂时不随便发
        asyncServiceExecutor.execute(new Runnable() {
            @Override
            public void run() {
                sendMail(recordDOList);
            }
        });
        asyncServiceExecutor.execute(new Runnable() {
            @Override
            public void run() {
                sendShortMessage(recordDOList);
            }
        });
    }

    /**
     * 发送短信
     *
     * @param recordDOList
     */
    private void sendShortMessage(List<WinningRecordDO> recordDOList) {
        if (CollectionUtils.isEmpty(recordDOList)) {
            logger.warn("中奖者名单为空");
            return;
        }
        recordDOList.forEach(record -> {//参数变量是record 类型是WinningRecordDO
            //创建出消息模板(json), 设置模板的属性
            Map<String, String> templateParam = new HashMap<>();
            templateParam.put("name", record.getWinnerName());
            templateParam.put("activityName", record.getActivityName());
            templateParam.put("prizeTiers", ActivityPrizeTiersEnum.forName(
                            record.getPrizeTier())
                    .getMessage()
            );
            templateParam.put("prizeName", record.getPrizeName());
            templateParam.put("winningTime", DateUtil.formatTime(record.getWinningTime()));

//            smsUtil.sendMessage(WINNING_TEMPLATE_CODE,
//                    record.getWinnerPhoneNumber().getValue(),
//                    JacksonUtil.writeValueAsString(templateParam));
        });
    }

    /**
     * 给中奖者发送邮件
     *
     * @param recordDOList
     */
    private void sendMail(List<WinningRecordDO> recordDOList) {
        if (CollectionUtils.isEmpty(recordDOList)) {
            logger.warn("中奖者名单为空");
            return;
        }
        recordDOList.forEach(record -> {//参数变量是record 类型是WinningRecordDO
            //消息模板
            String context = "Hi, " + record.getWinnerName() + ", 恭喜你在" +
                    record.getActivityName() + "活动中获得了" +
                    ActivityPrizeTiersEnum.forName(record.getPrizeTier()).getMessage() +
                    ": " + record.getPrizeName() +
                    ", 获奖时间是" + DateUtil.formatTime(record.getWinningTime()) +
                    ",请尽快领取您的奖励";
//            mailUtil.sendSampleMail(record.getWinnerEmail(),
//                    "中奖通知", context);
        });
    }


}
