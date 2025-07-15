package com.example.lotterysystem.service.activitystatus.operator;

import com.example.lotterysystem.dao.dataobject.ActivityDO;
import com.example.lotterysystem.dao.mapper.ActivityMapper;
import com.example.lotterysystem.dao.mapper.ActivityPrizeMapper;
import com.example.lotterysystem.service.dto.ActivityStatusConvertDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 活动状态处理对象
 */
@Component
public class ActivityOperator extends AbstractActivityOperator {
    @Autowired
    private ActivityMapper activityMapper;
    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;

    /**
     * 数字越小, 优先级越高
     * 因为活动要奖品都抽完, 奖品, 人员状态扭转好后,才去扭转活动状态
     *
     * @return
     */
    @Override
    public Integer sequence() {
        return 2;
    }

    /**
     * 判断活动是否更新
     *
     * @param statusConvertDTO 状态转换请求对象
     * @return
     */
    @Override
    public boolean needConvert(ActivityStatusConvertDTO statusConvertDTO) {
        //活动Id, 或者活动目标状态是空, 那么不需要转换更新
        if (null == statusConvertDTO.getActivityId() || null == statusConvertDTO.getActivityTargetStatus()) {
            return false;
        }
        //获取活动的信息, 关键信息是状态信息
        ActivityDO activityDO = activityMapper.selectById(statusConvertDTO.getActivityId());
        //找不到对应的活动
        if (activityDO == null) {
            return false;
        }
        String currentStatus = activityDO.getStatus();
        //状态一致, 不更新
        if (currentStatus.equals(statusConvertDTO.getActivityTargetStatus().name())) {
            return false;
        }
        //奖品还没有抽完, 不更新
        //查询活动没有被抽取的奖品数量
        if (activityPrizeMapper.countInitPrize(statusConvertDTO.getActivityId()) > 0) {
            return false;
        }
        //前面条件不符合, 说明可以状态转换
        return true;
    }

    /**
     * 处理活动状态转换的真正方法
     *
     * @param statusConvertDTO 状态转换请求对象
     * @return
     */
    @Override
    public boolean convertStatus(ActivityStatusConvertDTO statusConvertDTO) {
        try {
            activityMapper.updateStatus(statusConvertDTO.getActivityId(),
                    statusConvertDTO.getActivityTargetStatus().name());
            //没有出现异常错误的话, 那么就状态转换成功了
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
}
