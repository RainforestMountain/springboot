package com.example.lotterysystem.dao.mapper;

import com.example.lotterysystem.dao.dataobject.PrizeDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 奖品相关的数据库操作
 */
@Mapper
public interface PrizeMapper {

    @Insert("insert into prize(name, description, price, image_url)" +
            "values(#{name}, #{description}, #{price}, #{imageUrl})")
    //设置自增主键
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(PrizeDO prizeDO);

    @Select("select count(1) from prize")
    int count();

    /**
     * 翻页查询
     *
     * @param offset   数据偏移量, 查询起始量
     * @param pageSize 每页显示的记录数量（即每次查询返回的最大记录数）。
     * @return
     */
    @Select("select * from prize order by id desc limit #{offset}, #{pageSize}")
    List<PrizeDO> queryPrizesByPage(@Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    /**
     * 根据传递的奖品id列表获取存在的奖品id
     *
     * @param prizeIds
     * @return
     */
    List<Long> selectExistByIds(@Param("items") List<Long> prizeIds);

    /**
     * 根据传递的奖品id列表获取奖品的全部信息
     *
     * @param ids
     * @return
     */
    List<PrizeDO> batchSelectByIds(@Param("items") List<Long> ids);

    @Select("select * from prize where id = #{id}")
    PrizeDO selectById(@Param("id") Long id);
}
