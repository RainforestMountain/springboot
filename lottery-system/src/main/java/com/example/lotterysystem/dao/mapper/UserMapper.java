package com.example.lotterysystem.dao.mapper;

import com.example.lotterysystem.dao.dataobject.Encrypt;
import com.example.lotterysystem.dao.dataobject.UserDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 这⾥我们使⽤ MyBatis 来完成程序和数据库交互的框架
 * 一般一个表对应一个mapper, 模块化
 */
@Mapper
public interface UserMapper {

    /**
     * 添加新用户到数据库
     *
     * @param userDo
     * @return
     */
    @Insert("insert into user (user_name, email, phone_number, password, identity)" +
            "values (#{userName}, #{email}, #{phoneNumber}, #{password}, #{identity})")
    /**
     * 作用
     * 配置 MyBatis 在执行插入操作后，自动将数据库生成的主键值回写到 Java 对象中。
     * 这适用于使用自增主键（如 MySQL 的 AUTO_INCREMENT）或序列（如 Oracle 的 SEQUENCE）的场景。
     * 参数解析
     *
     * useGeneratedKeys = true
     * 启用主键自动获取功能。
     * MyBatis 会在插入后查询数据库生成的主键值。
     *
     * keyProperty = "id"
     * 指定 Java 对象中的属性名，用于存储生成的主键值。
     * 例如：User 类的 id 属性（private Integer id;）。
     *
     * keyColumn = "id"
     * 指定数据库表中的主键列名。
     * 通常与 keyProperty 对应，但当 Java 属性名与数据库列名不一致时需显式指定。
     */
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(UserDO userDo);

    /**
     * 获取手机号绑定的人数
     *
     * @param phoneNumber
     * @return
     * @Select 注解：
     * 这是 MyBatis 的核心 SQL 注解之一
     * 作用是定义一个查询 SQL 语句
     * 这里的 SQL 是 "select count (*) from user where phone_number = #{phoneNumber}"
     * 功能是统计 user 表中 phone_number 等于传入参数的记录数量
     * #{phoneNumber} 参数占位符：
     * MyBatis 会自动将方法参数注入到 SQL 中
     * 这里的 #{phoneNumber} 对应方法参数 @Param ("phoneNumber")
     * 注意它使用 #{} 而非 ${}，可以防止 SQL 注入
     * <p>
     * Encrypt 类的作用：
     * 这是一个自定义类型，用于封装加密数据
     * MyBatis 会通过 OGNL 表达式获取其 value 属性
     * 实际 SQL 执行时，#{phoneNumber} 会被替换为 encrypt.getValue () 返回的值
     * <p>
     * 方法返回值 int：
     * 对应 SQL 查询的结果 (count (*) 返回的整数)
     * MyBatis 会自动将查询结果映射为 int 类型
     * <p>
     * Java 方法参数名的丢失问题
     * Java 编译后，方法参数名默认会被擦除（如 countByPhoneNumber(Encrypt arg0)）。
     * 因此，MyBatis 无法直接通过反射获取原始参数名（如 phoneNumber）。
     * @Param("phoneNumber") 的作用是：
     * <p>
     * 参数命名：将方法参数与 SQL 中的占位符绑定。
     * 支持复杂类型：让 MyBatis 能正确访问自定义对象的属性。
     * 解决参数名丢失：避免 Java 编译后参数名被擦除的问题。
     */
    @Select("select count(*) from user where phone_number = #{phoneNumber}")
    int countByPhoneNumber(@Param("phoneNumber") Encrypt phoneNumber);

    /**
     * 获取邮箱绑定的人数
     *
     * @param email
     * @return
     */
    @Select("select count(*) from user where email = #{email}")
    int countByMail(@Param("email") String email);

    /**
     * 通过邮箱查询用户信息
     *
     * @param email
     * @return
     */

    UserDO selectByEmail(@Param("email") String email);

    /**
     * 通过手机号查询用户信息
     *
     * @param phoneNumber
     * @return
     */

    UserDO selectByPhoneNumber(@Param("phoneNumber") Encrypt phoneNumber);

    /**
     * 动态条件查询用户列表
     * 这个 selectUserList 方法的核心功能是：
     * 查询用户列表：从 user 表中检索数据。
     * 动态条件过滤：当传入的 identity 参数不为空时，添加 WHERE identity = #{identity} 条件。
     * 为空时,查询全部
     * 结果排序：无论是否有过滤条件，结果都按 id 字段降序排列。
     * 1. <script> 标签
     *
     * @param identity
     * @return
     * @Select("<script> ... </script>")
     * 作用：标记内部的内容为动态 SQL 片段，允许使用 MyBatis 的动态标签（如 <if>、<where> 等）。
     * 必要性：只有在需要使用动态 SQL 时才需要包裹 <script> 标签。若 SQL 是静态的（如无条件的固定查询），
     * 则无需使用。
     * 2.<if> 标签
     * <if test="identity != null">
     * WHERE identity = #{identity}
     * </if>
     * 作用：条件判断，仅当 test 属性中的表达式为 true 时，才会包含标签内的 SQL 片段。
     * 表达式解析：
     * identity != null：判断传入的 identity 参数是否不为空。
     * 注意：MyBatis 会自动将方法参数映射到 test 表达式中，无需额外处理。
     * 场景：常用于可选查询条件（如搜索框、筛选器等）。
     */
    List<UserDO> selectUserList(@Param("identity") String identity);

    /**
     * 查询存在的用户ID
     *
     * @param ids
     * @return
     */
    List<Long> selectExistByIds(@Param("items") List<Long> ids);

    /**
     * 通过中奖者的ids, 批量查询用户信息
     *
     * @param ids
     * @return
     */
    List<UserDO> batchSelectByIds(@Param("items") List<Long> ids);
}
