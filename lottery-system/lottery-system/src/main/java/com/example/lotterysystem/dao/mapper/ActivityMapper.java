package com.example.lotterysystem.dao.mapper;

import com.example.lotterysystem.dao.dataobject.ActivityDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 对应数据库的活动表
 */
@Mapper
public interface ActivityMapper {
    /**
     * 把活动信息(包括状态) 插入到数据库中, 但是没有把活动奖品列表, 活动人员列表插入数据库
     *
     * @param activityDO
     * @return
     */

    @Insert("insert into activity (activity_name, description, status)" +
            "values (#{activityName}, #{description}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(ActivityDO activityDO);

    /**
     * 统计活动总数
     *
     * @return
     */
    @Select("select  count(1) from activity")
    int count();

    /**
     * 翻页查询活动列表
     *
     * @param offset   数据偏移量, 查询起始量
     * @param pageSize 每页显示的记录数量（即每次查询返回的最大记录数）。
     * @return
     */
    @Select("select * from activity order by id desc limit #{offset}, #{pageSize}")
    List<ActivityDO> queryActivitiesByPage(@Param("offset") Integer offset, @Param("pageSize") Integer pageSize);


    /**
     * 通过活动id, 查询活动
     *
     * @param activityId
     * @return
     */
    @Select("select * from activity where id = #{activityId}")
    ActivityDO selectById(@Param("activityId") Long activityId);

    //@Update("update activity set status = #{status} where id = #{activityId}")
    void updateStatus(Long activityId, String status);
}
