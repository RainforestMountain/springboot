package com.example.lotterysystem.controller.param;

import com.example.lotterysystem.dao.dataobject.WinningRecordDO;
import com.example.lotterysystem.service.dto.WinningRecordDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 抽奖请求接口的前端参数, json格式
 */
@Data
public class DrawPrizeParam implements Serializable {
    /**
     * 中奖者名单, 一般请求和返回结果有列表的, 那么列表元素类型是静态内部类
     */
    @NotEmpty(message = "中奖用户名单不能为空")
    //校验列表里面的元素
    @Valid
    private List<Winner> winnerList;

    /**
     * 活动id
     */
    @NotNull(message = "活动id不能为空")
    private Long activityId;

    /**
     * 奖品id
     */
    @NotNull(message = "奖品id不能为空")
    private Long prizeId;

    /**
     * 中奖时间
     */
    @NotNull(message = "中奖时间不能为空")
    private Date winningTime;

    @Data
    public static class Winner implements Serializable {
        /**
         * 中奖用户id
         */
        @NotNull(message = "中奖用户id 不能为空")
        private Long userId;
        /**
         * 中奖用户姓名
         */
        @NotBlank(message = "中奖用户姓名不能为空")
        private String userName;
    }
}
