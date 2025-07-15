package com.example.lotterysystem.controller.result;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 展示中奖名单接口返回结果data部分
 */
@Data
public class WinningRecordResult implements Serializable {
    /**
     * 中奖者id
     */
    private Long winnerId;

    /**
     * 中奖者姓名
     */
    private String winnerName;

    /**
     * 奖品名称
     */
    private String PrizeName;

    /**
     * 奖品等级
     */
    private String prizeTier;

    /**
     * 中奖时间,格式是 "2024-05-21T11:55:10.000+00:00"
     */
    private Date winningTime;
}
