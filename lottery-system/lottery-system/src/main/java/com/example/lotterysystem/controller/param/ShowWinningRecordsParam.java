package com.example.lotterysystem.controller.param;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 查看中奖名单的参数对象
 */
@Data
public class ShowWinningRecordsParam implements Serializable {
    /**
     * 活动id
     */
    @NotNull(message = "活动id不能为空")
    private Long activityId;

    /**
     * 奖品id, 这个参数可以为空
     */
    private Long prizeId;
}
