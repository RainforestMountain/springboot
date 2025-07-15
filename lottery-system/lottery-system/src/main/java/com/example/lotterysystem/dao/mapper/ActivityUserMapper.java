package com.example.lotterysystem.dao.mapper;

import com.example.lotterysystem.dao.dataobject.ActivityUserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 对应数据库的活动人员关联表
 */
@Mapper
public interface ActivityUserMapper {
    /**
     * 把活动人员列表的每一条数据插入到活动人员关联表
     *
     * @param activityUserDOList
     * @return
     */

    int batchInsert(@Param("items") List<ActivityUserDO> activityUserDOList);

    @Select("select * from activity_user where activity_id = #{activityId}")
    List<ActivityUserDO> selectByActivityId(Long activityId);

    @Select("select * from activity_user where activity_id = #{activityId} and user_id = #{userId}")
    ActivityUserDO selectByActivityAndUserId(Long activityId, Long userId);


    void batchUpdateStatus(@Param("activityId") Long activityId,
                           @Param("userIds") List<Long> userIds,
                           @Param("status") String status);
}

