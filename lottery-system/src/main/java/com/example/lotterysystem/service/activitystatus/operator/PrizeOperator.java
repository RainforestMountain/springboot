package com.example.lotterysystem.service.activitystatus.operator;

import com.example.lotterysystem.dao.dataobject.ActivityPrizeDO;
import com.example.lotterysystem.dao.mapper.ActivityPrizeMapper;
import com.example.lotterysystem.service.dto.ActivityStatusConvertDTO;
import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 活动奖品状态处理对象
 */
@Component
public class PrizeOperator extends AbstractActivityOperator {
    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;

    /**
     * 奖品处理器优先级较高
     *
     * @return
     */
    @Override
    public Integer sequence() {
        return 1;
    }

    /**
     * 判断活动的奖品状态是否转换
     *
     * @param statusConvertDTO 状态转换请求对象
     * @return
     */
    @Override
    public boolean needConvert(ActivityStatusConvertDTO statusConvertDTO) {
        //活动奖品Id, 或者活动奖品目标状态是空, 那么不需要转换更新
        if (null == statusConvertDTO.getPrizeId() || null == statusConvertDTO.getPrizeTargetStatus()) {
            return false;
        }
        //获取活动奖品的信息, 包括状态信息, 这里是查找当个奖品, 要活动id, 奖品id
        ActivityPrizeDO activityPrizeDO = activityPrizeMapper.selectByActivityAndPrizeId(
                statusConvertDTO.getActivityId(), statusConvertDTO.getPrizeId()
        );
        //没找到对应的活动奖品
        if (null == activityPrizeDO) {
            return false;
        }
        // 当前状态与⽬标状态不⼀致才更新，这⾥可以再继续校验状态在何种状态下可以扭转。
        // 例如INIT可以转COMPLETED，⽽COMPLETED不能转INIT...
        //当前奖品状态是completed了, 不能状态转换
        String currentStatus = activityPrizeDO.getStatus();
        if (currentStatus.equals(statusConvertDTO.getPrizeTargetStatus().name())
                || currentStatus.equals(ActivityPrizeStatusEnum.COMPLETED.name())) {
            return false;
        }

        return true;
    }

    /**
     * 活动奖品的状态转换操作, 涉及数据库
     *
     * @param statusConvertDTO 状态转换请求对象
     * @return
     */
    @Override
    public boolean convertStatus(ActivityStatusConvertDTO statusConvertDTO) {
        try {
            activityPrizeMapper.updateStatus(
                    statusConvertDTO.getActivityId(),
                    statusConvertDTO.getPrizeId(),
                    statusConvertDTO.getPrizeTargetStatus().name()
            );
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
}
