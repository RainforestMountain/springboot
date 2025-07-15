package com.example.lotterysystem.dao.dataobject;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class PrizeDO extends BaseDO {
    /**
     * 奖品名称
     */
    private String name;

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
