package com.example.lotterysystem.service;

import com.example.lotterysystem.controller.param.CreateActivityParam;
import com.example.lotterysystem.controller.param.PageListParam;
import com.example.lotterysystem.service.dto.ActivityDTO;
import com.example.lotterysystem.service.dto.ActivityDetailDTO;
import com.example.lotterysystem.service.dto.CreateActivityDTO;
import com.example.lotterysystem.service.dto.PageListDTO;

public interface ActivityService {

    /**
     * 创建活动
     *
     * @param request 控制层传递的请求
     * @return 返回结果 DTO
     */
    CreateActivityDTO createActivity(CreateActivityParam request);


    /**
     * 翻页查找活动列表
     *
     * @param request
     * @return
     */
    PageListDTO<ActivityDTO> findActivityList(PageListParam request);

    /**
     * 获取单个活动和关联奖品的细节信息
     *
     * @param activityId
     * @return
     */
    ActivityDetailDTO getActivityDetail(Long activityId);

    /**
     * 活动相关状态扭转的原版接口
     *
     * @param activityId
     * @param activityTargetStatus
     * @param prizeId
     * @param prizeTargetStatus
     */
    void reverseStatus(Long activityId, String activityTargetStatus,
                       Long prizeId, String prizeTargetStatus);

    /**
     * 通过活动id 缓存活动完整信息到redis
     *
     * @param activityId
     */
    void cacheActivity(Long activityId);

    /**
     * 缓存活动完整信息
     *
     * @param detailDTO
     */
    void cacheActivity(ActivityDetailDTO detailDTO);
}


