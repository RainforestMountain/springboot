package com.example.lotterysystem.controller.result;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 查询活动列表接口返回结果, data
 */
@Data
public class FindActivityListResult implements Serializable {
    /**
     * 总数
     */
    private int total;
    /**
     * 当前页的数据
     */
    private List<ActivityInfo> records;

    /**
     * 静态内部类,更好的去展示信息
     */
    @Data
    public static class ActivityInfo implements Serializable {
        /**
         * 活动id
         */
        private Long activityId;
        /**
         * 活动名
         */
        private String activityName;

        /**
         * 活动描述
         */
        private String description;

        /**
         * 活动目前是否生效
         */
        private boolean isValid;
    }
}
