package com.example.lotterysystem.controller.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 创建奖品接口, 前端给后端传递的参数, 肯定要进行有效性检验
 */
@Data
public class CreatePrizeParam implements Serializable {

    /**
     * 奖品名称
     */
    @NotBlank(message = "奖品名称不能为空")
    private String prizeName;

    /**
     * 奖品描述, 可以为空
     */
    private String description;

    /**
     * 奖品价值
     */
    @NotNull(message = "奖品价值不能为空")
    private BigDecimal price;
}
