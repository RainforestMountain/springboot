package com.example.lotterysystem.service;

import com.example.lotterysystem.controller.param.DrawPrizeParam;
import com.example.lotterysystem.controller.param.ShowWinningRecordsParam;
import com.example.lotterysystem.dao.dataobject.WinningRecordDO;
import com.example.lotterysystem.service.dto.WinningRecordDTO;

import java.util.List;

/**
 * 处理抽奖信息请求
 * 不涉及控制层的交互, 所以没有DTO
 */
public interface DrawPrizeService {
    /**
     * 异步抽奖, 接口只做奖品数校验即可返回, 具体实现
     * 要给rabbitmq推送抽奖请求消息, 让mq完成异步处理的一些操作
     *
     * @param param
     */
    void drawPrize(DrawPrizeParam param);

    /**
     * 不涉及与控制层的交互
     * 核对校验抽奖信息有效性
     * 校验什么？
     * 校验是否存在活动奖品• 校验奖品数量是否⾜够中奖⼈数
     * 校验活动有效性
     * 校验抽取的奖品的有效性
     * ....
     *
     * @param param
     */
    void checkDrawPrizeValid(DrawPrizeParam param);

    /**
     * 根据抽奖信息保存中奖记录
     *
     * @param param
     * @return
     */
    List<WinningRecordDO> saveWinningRecords(DrawPrizeParam param);

    /**
     * 根据活动或者奖品id查询对应的中奖信息, 用于判断中奖记录是否落库
     * 用于回滚操作
     *
     * @param param
     * @return
     */
    List<WinningRecordDTO> showWinningRecords(ShowWinningRecordsParam param);

    /**
     * 清除中奖记录(库+ 缓存)
     *
     * @param activityId
     * @param prizeId
     * @param winnerIds
     */
    void removeRecords(Long activityId, Long prizeId, List<Long> winnerIds);
}
