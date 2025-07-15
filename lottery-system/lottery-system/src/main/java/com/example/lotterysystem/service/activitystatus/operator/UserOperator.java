package com.example.lotterysystem.service.activitystatus.operator;

import com.example.lotterysystem.dao.dataobject.ActivityUserDO;
import com.example.lotterysystem.dao.mapper.ActivityUserMapper;
import com.example.lotterysystem.service.dto.ActivityStatusConvertDTO;
import com.example.lotterysystem.service.enums.ActivityUserStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * 活动人员状态处理对象
 */
@Component
public class UserOperator extends AbstractActivityOperator {
    @Autowired
    private ActivityUserMapper activityUserMapper;

    /**
     * 活动人员处理器的优先级
     *
     * @return
     */
    @Override
    public Integer sequence() {
        return 1;
    }

    /**
     * 活动人员是否需要状态转换
     *
     * @param statusConvertDTO 状态转换请求对象
     * @return
     */
    @Override
    public boolean needConvert(ActivityStatusConvertDTO statusConvertDTO) {
        //活动人员id列表是空, 或者目标状态时空, 不进行状态转换
        if (CollectionUtils.isEmpty(statusConvertDTO.getUserIds())
                || null == statusConvertDTO.getUserTargetStatus()) {
            return false;
        }
        //针对通过人员id列表从数据库查询到的人员信息(状态)列表进行判断

        for (Long userId : statusConvertDTO.getUserIds()) {
            ActivityUserDO activityUserDO = activityUserMapper.selectByActivityAndUserId(
                    statusConvertDTO.getActivityId(), userId);
            if (null == activityUserDO) {
                return false;
            }
            // 当前状态与⽬标状态不⼀致才更新，这⾥可以再继续校验状态在何种状态下可以扭转。
            // 例如INIT可以转COMPLETED，⽽COMPLETED不能转INIT...
            String currentStatus = activityUserDO.getStatus();
            if (currentStatus.equals(statusConvertDTO.getActivityTargetStatus().name())) {
                return false;
            }
            if (currentStatus.equals(ActivityUserStatusEnum.COMPLETED.name())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean convertStatus(ActivityStatusConvertDTO statusConvertDTO) {
        try {
            activityUserMapper.batchUpdateStatus(
                    statusConvertDTO.getActivityId(),
                    statusConvertDTO.getUserIds(),
                    statusConvertDTO.getActivityTargetStatus().name()
            );
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
}
