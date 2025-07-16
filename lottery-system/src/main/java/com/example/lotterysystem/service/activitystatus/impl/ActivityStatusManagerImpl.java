package com.example.lotterysystem.service.activitystatus.impl;

import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.service.ActivityService;
import com.example.lotterysystem.service.activitystatus.ActivityStatusManager;
import com.example.lotterysystem.service.activitystatus.operator.AbstractActivityOperator;
import com.example.lotterysystem.service.dto.ActivityStatusConvertDTO;
import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import com.example.lotterysystem.service.enums.ActivityUserStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Slf4j
@Service
public class ActivityStatusManagerImpl implements ActivityStatusManager {
    private static final Logger logger =
            LoggerFactory.getLogger(ActivityStatusManagerImpl.class);

    /**
     * 责任链的维护, operatorMap, 是一个包含所有处理者对象的映射, 按照sequence()方法返回处理者的顺序维护了责任链
     */
    @Autowired
    private Map<String, AbstractActivityOperator> operatorMap = new HashMap<>();
    @Autowired
    private ActivityService activityService;

    /**
     * 对应活动相关的状态转换请求的处理方法
     * 其中还会调用, 实现责任链的方法processStatusConversion
     * 由于状态转换涉及数据库的操作,而且不止一次, 所有要使用事务, 保证能够及时回滚
     *
     * @param statusConvertDTO 状态转换请求对象
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void handleEvent(ActivityStatusConvertDTO statusConvertDTO) {
        //operatorMap 是空, 没有处理器
        if (CollectionUtils.isEmpty(operatorMap)) {
            logger.info("AbstractActivityOperatorMap 是空");
            return;
        }

        Map<String, AbstractActivityOperator> currMap = new HashMap<>(operatorMap);
        //判断状态是否更新
        boolean update = false;

        //1.先扭转的状态, 使用优先级, 并非具体处理器的名称或者类型, 有更高的灵活性
        //需要改变处理者对象组成的链的处理器的顺序的时候, 只需要去改变处理器的优先级就行, 即改变处理器的代码, 不需要改变其他的代码
        update = processStatusConversion(statusConvertDTO, currMap, 1) || update;
        //2.后扭转的状态(分开是因为要依赖先扭转的状态)
        update = processStatusConversion(statusConvertDTO, currMap, 2) || update;
        //3.判断状态是否更新从而更新缓存
        //由于创建活动时, 将活动信息存放到redis中, 因此需要修改活动更新缓存
        if (update) {
            //缓存活动数据
            activityService.cacheActivity(statusConvertDTO.getActivityId());
        }
    }

    /**
     * 活动状态回滚的处理方法
     * 使用spring事务, 保证一致性
     *
     * @param statusConvertDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackHandleEvent(ActivityStatusConvertDTO statusConvertDTO) {
        //扭转状态
        //还是使用处理器, 责任链模式
        //operatorMap：活动、奖品、人员
        // 活动是否需要回滚？？ 绝对需要，
        // 原因：奖品都恢复成INIT，那么这个活动下的奖品绝对没抽完

        for (AbstractActivityOperator operator : operatorMap.values()) {
            //创建状态回滚的请求对象
            ActivityStatusConvertDTO rollbackDTO =
                    buildRollbackConvertDTO(statusConvertDTO);
            //处理器的状态转换, 是每个状态都会转换,
            //在这里, needConvert设置的是, 有的状态返回不需要转换
            operator.convertStatus(rollbackDTO);
        }
        //数据库的活动相关的信息已经回滚了, 从数据库中读取活动相关的信息并且更新缓存
        activityService.cacheActivity(statusConvertDTO.getActivityId());
    }

    /**
     * //创建状态回滚的请求对象
     *
     * @param statusConvertDTO
     * @return
     */
    private ActivityStatusConvertDTO buildRollbackConvertDTO(ActivityStatusConvertDTO statusConvertDTO) {
        ActivityStatusConvertDTO rollbackDTO = new ActivityStatusConvertDTO();
        rollbackDTO.setActivityId(statusConvertDTO.getActivityId());
        //回滚对象设置目标状态为正在进行状态
        rollbackDTO.setActivityTargetStatus(ActivityStatusEnum.RUNNING);
        rollbackDTO.setPrizeId(statusConvertDTO.getPrizeId());
        rollbackDTO.setPrizeTargetStatus(ActivityPrizeStatusEnum.INIT);
        rollbackDTO.setUserIds(statusConvertDTO.getUserIds());
        rollbackDTO.setUserTargetStatus(ActivityUserStatusEnum.INIT);
        return rollbackDTO;
    }

    /**
     * 请求的传递：在 processStatusConversion ⽅法中，通过迭代器遍历 operatorMap，
     * 对每个操作符实例调⽤needConvert⽅法来判断是否需要由当前操作符处理请求
     * 处理请求：如果 needConvert 返回true，则调⽤ convertStatus ⽅法来处理请求。
     * 终⽌责任链：⼀旦请求被某个操作符处理，迭代器中的该操作符将被移除（it.remove()），
     * 这防⽌了请求被重复处理，并且终⽌了对该操作符的责任链
     * 异常处理：如果在责任链中的任何点上请求处理失败（convertStatus返回false），
     * 则抛出异常，这可以看作是责任链的终⽌
     *
     * @param statusConvertDTO 状态转换请求对象
     * @param currOperaterMap  处理对象集合的映射
     * @param sequence         处理器的优先级
     * @return
     */
    private boolean processStatusConversion(ActivityStatusConvertDTO statusConvertDTO,
                                            Map<String, AbstractActivityOperator> currOperaterMap,
                                            int sequence) {
        //判断这个处理器对应的活动那部分模块的状态是否更新
        boolean update = false;

        //迭代器, 用于遍历currOperatorMap
        Iterator<Map.Entry<String, AbstractActivityOperator>> it = currOperaterMap.entrySet().iterator();

        while (it.hasNext()) {
            AbstractActivityOperator operator = it.next().getValue();
            //优先级不匹配,或者匹配了, 但是判断后,不需要转换
            if (operator.sequence() != sequence
                    || !operator.needConvert(statusConvertDTO)) {
                continue;
            }
            //到这里需要转换了, 如果没有转换成功, 抛出自定义的服务层异常
            if (!operator.convertStatus(statusConvertDTO)) {//这一步就开始真正的状态转换了
                //打印出处理器的类名
                logger.error("状态转换失败, operator: {}", operator.getClass().getName());
                throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_STATUS_CONVERT_ERROR);
            }
            //没有抛出异常, 说明转换操作成功了
            update = true;
            it.remove();
        }
        return update;
    }

}
