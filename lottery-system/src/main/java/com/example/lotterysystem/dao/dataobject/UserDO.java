package com.example.lotterysystem.dao.dataobject;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Dao层的人员信息对象, 与数据库密切相关
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserDO extends BaseDO {
    /**
     * 用户名
     */
    private String userName;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 手机号
     */
    private Encrypt phoneNumber;
    /**
     * 密码
     */
    private String password;
    /**
     * 用户身份
     */
    private String identity;
}
