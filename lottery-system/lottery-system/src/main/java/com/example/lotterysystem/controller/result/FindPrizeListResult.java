package com.example.lotterysystem.controller.result;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 奖品列表展示接口的返回结果(data)
 */
@Data
public class FindPrizeListResult implements Serializable {
    /**
     * 总数
     */
    private int total;

    /**
     * 多个页的数据, 有很多, 表示为一个List
     */
    private List<PrizeInfo> records;

    @Data
    public static class PrizeInfo implements Serializable {
        /**
         * 奖品id
         */
        private Long prizeId;

        /**
         * 奖品名称
         */
        private String prizeName;

        /**
         * 奖品描述
         */
        private String description;

        /**
         * 奖品价值
         */
        private BigDecimal price;

        /**
         * 奖品图
         * 由于数据库没有文件类型
         * 所有使用文件路径来表示
         */
        private String imageUrl;
    }
}
