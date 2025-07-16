package com.example.lotterysystem.controller.param;

import com.example.lotterysystem.service.enums.ActivityPrizeTiersEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 活动关联的奖品信息参数
 */
@Data
public class CreatePrizeByActivityParam {
    /**
     * 奖品id
     */
    @NotNull(message = "奖品id不为空")
    private Long prizeId;

    /**
     * 奖品数量
     */
    @NotNull(message = "奖品数量不能为空")
    private Long prizeAmount;

    /**
     * 奖品等级
     * 使用枚举类, 方便用更多形式表示, 更好去维护有拓展性
     * 字符串全靠打字, 有很多不确定性
     */
    @NotBlank(message = "奖品等奖不能为空！")
    private String prizeTiers;
}
