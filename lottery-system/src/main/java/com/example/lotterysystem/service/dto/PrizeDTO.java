package com.example.lotterysystem.service.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 奖品信息, 不同层次的奖品信息对象的属性有一定的差异,因为有些不需要
 */
@Data
public class PrizeDTO implements Serializable {
    /**
     * 奖品id
     */
    private Long id;
    /**
     * 奖品名称
     */
    private String name;
    /**
     * 检品描述
     */
    private String description;
    /**
     * 奖品价值
     */
    private BigDecimal price;
    /**
     * 奖品图
     */
    private String imageUrl;
}
