package com.example.lotterysystem.dao.mapper;

import com.example.lotterysystem.dao.dataobject.ActivityPrizeDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 对标的是数据库的活动奖品关联表
 */
@Mapper
public interface ActivityPrizeMapper {

    /**
     * 把活动奖品列表插入到数据库的活动奖品关联表, mysql没有列表List这样的类型
     *
     * @param activityPrizeDOList
     * @return
     */
    int batchInsert(@Param("items") List<ActivityPrizeDO> activityPrizeDOList);

    /**
     * 通过活动id, 奖品id, 查询活动奖品信息
     *
     * @param activityId
     * @param prizeId
     * @return
     */
    @Select("select * from activity_prize where activity_id = #{activityId} and prize_id = #{prizeId}")
    ActivityPrizeDO selectByActivityAndPrizeId(@Param("activityId") Long activityId,
                                               @Param("prizeId") Long prizeId);

    @Select("select * from activity_prize where activity_id = #{activityId}")
    List<ActivityPrizeDO> selectByActivityId(Long activityId);

    @Select("select count(1) from activity_prize where activity_id = #{activityId} and status ='INIT'")
    int countInitPrize(Long activityId);

    //把属性故意写错, 检测数据库的回滚能力
    @Update("update activity_prize set status = #{status} where activity_id = #{activityId} and prize_id = #{prizeId}")
    void updateStatus(Long activityId, Long prizeId, String status);
}
