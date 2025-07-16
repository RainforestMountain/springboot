package com.example.lotterysystem.dao.mapper;

import com.example.lotterysystem.dao.dataobject.WinningRecordDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface WinningRecordMapper {

    void deleteRecordByWinners(Long activityId, Long prizeId, List<Long> winnerIds);

    /**
     * 删除活动 或 奖品下的中奖记录
     *
     * @param activityId
     * @param prizeId
     */
    void deleteRecords(Long activityId, Long prizeId);


    List<WinningRecordDO> findRecordListByA(Long activityId);

    /**
     *
     * @param activityId 不能为空
     * @param prizeId 可以为空
     * @return
     */
    List<WinningRecordDO> findRecordListByAp(Long activityId, Long prizeId);


    void batchInsert(List<WinningRecordDO> winningRecordDOList);


    int countByAPId(@Param("activityId") Long activityId, @Param("prizeId") Long prizeId);
}
