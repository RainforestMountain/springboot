package com.example.lotterysystem.service.activitystatus;

import com.example.lotterysystem.service.dto.ActivityStatusConvertDTO;

/**
 * 活动状态管理接口 ActivityStatusManager
 */
public interface ActivityStatusManager {
    /**
     * 活动状态转换
     *
     * @param statusConvertDTO
     */
    void handleEvent(ActivityStatusConvertDTO statusConvertDTO);

    /**
     * 活动状态回滚
     *
     * @param statusConvertDTO
     */
    void rollbackHandleEvent(ActivityStatusConvertDTO statusConvertDTO);
}
