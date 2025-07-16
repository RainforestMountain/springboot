package com.example.lotterysystem.service.impl;

import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.common.utils.RedisUtil;
import com.example.lotterysystem.controller.param.DrawPrizeParam;
import com.example.lotterysystem.controller.param.ShowWinningRecordsParam;
import com.example.lotterysystem.dao.dataobject.*;
import com.example.lotterysystem.dao.mapper.*;
import com.example.lotterysystem.service.DrawPrizeService;
import com.example.lotterysystem.service.dto.WinningRecordDTO;
import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityPrizeTiersEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.lotterysystem.common.config.DirectRabbitConfig.EXCHANGE_NAME;
import static com.example.lotterysystem.common.config.DirectRabbitConfig.ROUTING;

@Service
public class DrawPrizeServiceImpl implements DrawPrizeService {
    private static final Logger logger = LoggerFactory.getLogger(DrawPrizeServiceImpl.class);
    /**
     * 中奖记录的前缀
     */
    private static final String WINNING_RECORD_PREFIX = "WINNING_RECORD_";

    /**
     * 抽奖活动有效时间(s)
     */
    private static final long ACTIVITY_RECORD_EFFECTIVE_TIME = 60 * 60 * 24 * 2;

    /**
     * 奖品有效时间(s)
     */
    private static final long PRIZE_RECORD_EFFECTIVE_TIME = 60 * 60 * 24;
    /**
     * 使用rabbitTemplate, 这里提供了接收/发送等方法
     */
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private WinningRecordMapper mapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PrizeMapper prizeMapper;
    @Autowired
    private WinningRecordMapper winningRecordMapper;
    @Autowired
    private RedisUtil redisUtil;


    /**
     * 抽奖请求消息具体处理实现
     * 发送到mq
     *
     * @param param
     */
    @Override
    public void drawPrize(DrawPrizeParam param) {
        //将中奖消息发送mq进行异步处理
        String messageId = String.valueOf(UUID.randomUUID());//随机的UUID
        String messageData = JacksonUtil.writeValueAsString(param);//消息数据格式是json
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        //消息通常是⼀个带有⼀定业务
        //逻辑结构的数据, ⽐如JSON字符串. 消息可以带有⼀定的标签, RabbitMQ会根据标签进⾏路由, 把消
        //息发送给感兴趣的消费者(Consumer)
        Map<String, String> map = new HashMap<>();
        map.put("messageId", messageId);
        map.put("messageData", messageData);
        map.put("createTime", createTime);

        //将消息携带绑定键值: DirectRouting发送到DirectExchange,
        //这样，发往 directExchange 、路由键匹配 ROUTING 的消息，
        // 会被路由到 directQueue 。
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING, map);
        logger.info("发送mq完成");

    }

    /**
     * 校验抽奖信息有效性的业务代码实现
     *
     * @param param
     */
    @Override
    public void checkDrawPrizeValid(DrawPrizeParam param) {
        ActivityPrizeDO activityPrizeDO = activityPrizeMapper.selectByActivityAndPrizeId(param.getActivityId(), param.getPrizeId());
        ActivityDO activityDO = activityMapper.selectById(param.getActivityId());
        //校验是否存在活动奖品
        if (null == activityPrizeDO || null == activityDO) {
            throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_OR_PRIZE_IS_EMPTY);
        }
        //校验奖品数量是否足够中奖人数
        if (param.getWinnerList().size() > activityPrizeDO.getPrizeAmount()) {
            throw new ServiceException(ServiceErrorCodeConstants.WINNER_PRIZE_AMOUNT_ERROR);
        }
        //校验活动有效性(利用状态)
        if (activityDO.getStatus().equals(ActivityStatusEnum.COMPLETED.name())) {
            throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_COMPLETED);
        }
        //校验抽取的奖品的有效性(利用状态)
        if (activityPrizeDO.getStatus().equals(ActivityPrizeStatusEnum.COMPLETED.name())) {
            throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_PRIZE_COMPLETED);
        }
    }

    @Override
    public List<WinningRecordDO> saveWinningRecords(DrawPrizeParam param) {
        //1.查询相关信息: 中奖者, 活动, 奖品
        //活动被抽取的奖品
        ActivityPrizeDO activityPrizeDO =
                activityPrizeMapper.selectByActivityAndPrizeId(param.getActivityId(), param.getPrizeId());
        //抽奖活动信息
        ActivityDO activityDO = activityMapper.selectById(param.getActivityId());
        //中奖者id列表, 从前端请求获取处理
        List<Long> winnerIds = param.getWinnerList().stream()
                .map(DrawPrizeParam.Winner::getUserId)
                .toList();
        //通过中奖者的id查询中奖者的完整信息
        List<UserDO> userDos = userMapper.batchSelectByIds(winnerIds);
        //查询被抽取奖品的完整信息
        PrizeDO prizeDo = prizeMapper.selectById(param.getPrizeId());

        //2.整合用户中奖记录
        //用户中奖记录列表
        List<WinningRecordDO> winningRecordDOList = userDos.stream()
                .map(userDO -> {
                    WinningRecordDO winningRecordDO = new WinningRecordDO();
                    winningRecordDO.setWinnerId(userDO.getId());
                    winningRecordDO.setWinnerName(userDO.getUserName());
                    winningRecordDO.setWinnerEmail(userDO.getEmail());
                    winningRecordDO.setWinnerPhoneNumber(userDO.getPhoneNumber());
                    winningRecordDO.setActivityId(activityDO.getId());
                    winningRecordDO.setActivityName(activityDO.getActivityName());
                    winningRecordDO.setPrizeId(prizeDo.getId());
                    winningRecordDO.setPrizeName(prizeDo.getName());
                    winningRecordDO.setPrizeTier(activityPrizeDO.getPrizeTiers());
                    winningRecordDO.setWinningTime(param.getWinningTime());
                    return winningRecordDO;
                }).toList();
        //每次中奖结果都要持久化到mysql
        winningRecordMapper.batchInsert(winningRecordDOList);
        //3.将中奖结果存入redis中
        //单一中奖结果
        saveRecordsToCache(param.getActivityId() + "_" + param.getPrizeId(), winningRecordDOList, PRIZE_RECORD_EFFECTIVE_TIME);

        //4.如果活动已经完成了, 那么把整个活动的记录统一存储一遍
        if (activityDO.getStatus().equals(ActivityStatusEnum.COMPLETED.name())) {
            List<WinningRecordDO> allList = winningRecordMapper.findRecordListByA(param.getActivityId());
            saveRecordsToCache(String.valueOf(param.getActivityId()), allList, ACTIVITY_RECORD_EFFECTIVE_TIME);
        }
        return winningRecordDOList;
    }

    /**
     * 根据活动或者奖品id查询对应的中奖信息, 用于判断中奖记录是否落库的具体业务代码实现
     * 用于回滚操作
     *
     * @param param
     * @return
     */
    @Override
    public List<WinningRecordDTO> showWinningRecords(ShowWinningRecordsParam param) {
        //从缓存中获取, redis是很快的
        //创建一个获取中奖信息的key
        //这个key其实和这个类里面的saveWinningRecords方法中redis的key一致

        String key = (null == param.getPrizeId()
                ? String.valueOf(param.getActivityId()) //还没有开始抽奖或者已经抽完
                : param.getActivityId() + "_" + param.getPrizeId());
        //是一个与redis缓存相关的方法
        //WinningRecordDO 与数据库相关的对象
        List<WinningRecordDO> winningRecordDOList = getRecordsFromCache(key);
        //WinningRecordDTO 服务层返回和传递的对象
        List<WinningRecordDTO> winningRecordDTOList = null;
        //获取的中奖记录列表不是空列表
        if (!CollectionUtils.isEmpty(winningRecordDOList)) {
            winningRecordDTOList = covertRecordToDTO(winningRecordDOList);
            return winningRecordDTOList;
        }

        //从数据库中获取
        if (null == param.getPrizeId()) {
            //抽奖请求的奖品id是空, 可能没有开始抽奖或者或者已经抽完
            winningRecordDOList = winningRecordMapper.findRecordListByA(param.getActivityId());
        } else {
            winningRecordDOList = winningRecordMapper.findRecordListByAp(param.getActivityId(),
                    param.getPrizeId());
        }

        //获取到了中奖记录列表
        if (CollectionUtils.isEmpty(winningRecordDOList)) {
            logger.warn("showWinningRecords 获取中奖记录是空! param: {}",
                    JacksonUtil.writeValueAsString(param));
            //返回一个空列表
            return Arrays.asList();
        }
        //不是空, 那么创建服务层的状态回滚请求对象列表, 通过转换方法获取
        winningRecordDTOList = covertRecordToDTO(winningRecordDOList);
        //记录存入缓存,回滚更新缓存
        //如果奖品抽完了或者没抽, 有效时间是活动有效时间
        //否则, 有效时间是奖品有效时间
        saveRecordsToCache(key, winningRecordDOList,
                null != param.getPrizeId() ? PRIZE_RECORD_EFFECTIVE_TIME : ACTIVITY_RECORD_EFFECTIVE_TIME);
        return winningRecordDTOList;
    }


    @Override
    public void removeRecords(Long activityId, Long prizeId, List<Long> winnerIds) {
        if (null == activityId) {
            logger.error("要删除中奖记录的活动id为空");
            return;
        }
        //删除数据库中的中奖记录(这次抽奖请求操作)
        if (!CollectionUtils.isEmpty(winnerIds)) {
            //中奖者id不是空列表
            winningRecordMapper.deleteRecordByWinners(activityId, prizeId, winnerIds);
        } else {
            //这次没有中奖者, 依然要回滚
            winningRecordMapper.deleteRecords(activityId, prizeId);
        }

        //删缓存
        //还有奖品被抽取的情况
        if (null != prizeId) {
            deleteRecordsToCache(activityId + "_" + prizeId);
        }
        //没有奖品被抽取情况, 确保更完备
        deleteRecordsToCache(String.valueOf(activityId));
    }


    /**
     * dao层转换到服务层
     *
     * @param winningRecordDOList
     * @return
     */
    private List<WinningRecordDTO> covertRecordToDTO(List<WinningRecordDO> winningRecordDOList) {
        if (CollectionUtils.isEmpty(winningRecordDOList)) {
            return Arrays.asList();
        }
        return winningRecordDOList.stream()
                .map(winningRecordDO -> {
                    WinningRecordDTO winningRecordDTO = new WinningRecordDTO();
                    winningRecordDTO.setWinnerId(winningRecordDO.getWinnerId());
                    winningRecordDTO.setWinnerName(winningRecordDO.getWinnerName());
                    winningRecordDTO.setPrizeName(winningRecordDO.getPrizeName());
                    winningRecordDTO.setPrizeTier(
                            ActivityPrizeTiersEnum.forName(winningRecordDO.getPrizeTier()));
                    winningRecordDTO.setWinningTime(winningRecordDO.getWinningTime());
                    return winningRecordDTO;
                }).collect(Collectors.toList());
    }

    /**
     * 缓存中奖记录
     *
     * @param key
     * @param recordDOList
     * @param time
     */
    public void saveRecordsToCache(String key, List<WinningRecordDO> recordDOList, long time) {
        try {
            if (CollectionUtils.isEmpty(recordDOList)) {
                logger.warn("缓存的中奖记录是空, 不进行缓存, Key:{}", key);
                return;
            }
            String records = JacksonUtil.writeValueAsString(recordDOList);
            redisUtil.set(WINNING_RECORD_PREFIX + key, records, time);
        } catch (Exception e) {
            logger.error("缓存中奖记录异常, key:{}", key, e);
        }
    }

    /**
     * 从缓存中获取中奖记录
     *
     * @param key
     * @return
     */
    public List<WinningRecordDO> getRecordsFromCache(String key) {
        try {
            //没有这个键
            if (!redisUtil.hasKey(WINNING_RECORD_PREFIX + key)) {
                //比如 WINNING_RECORD_36
                logger.warn("缓存信息不存在, key:{}", WINNING_RECORD_PREFIX + key);
                return Arrays.asList();
            }
            String records = (String) redisUtil.get(WINNING_RECORD_PREFIX + key);
            //拿到的是json字符串
            return JacksonUtil.readListValue(records, WinningRecordDO.class);
        } catch (Exception e) {
            logger.error("获取中奖记录异常, key:{}", key, e);
            return Arrays.asList();
        }
    }

    /**
     * 删除缓存
     *
     * @param key
     */
    public void deleteRecordsToCache(String key) {
        try {
            //没有这个键
            if (!redisUtil.hasKey(WINNING_RECORD_PREFIX + key)) {
                logger.warn("缓存信息不存在, 不用删除, key:{}", WINNING_RECORD_PREFIX + key);
                return;
            }
            //删除中奖记录
            redisUtil.del(WINNING_RECORD_PREFIX + key);
        } catch (Exception e) {
            logger.error("删除中奖记录异常,key:{}", key, e);
        }
    }
}
