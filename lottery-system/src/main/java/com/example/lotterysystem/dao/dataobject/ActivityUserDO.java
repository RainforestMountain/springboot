package com.example.lotterysystem.dao.dataobject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ActivityUserDO extends BaseDO {
    /**
     * 活动id
     */
    private Long activityId;

    /**
     * 圈选的人员id
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 人员状态
     *
     * @See ActivityUserStatusEnum#name()
     */
    private String status;
}
