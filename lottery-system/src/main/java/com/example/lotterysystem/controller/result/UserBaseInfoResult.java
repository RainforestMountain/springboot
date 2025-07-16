package com.example.lotterysystem.controller.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 查找人员列表, 服务器的返回结果, 一般是统一返回结果的data部分
 */
@Data
public class UserBaseInfoResult implements Serializable {
    /**
     * 用户id
     */
    private Long UserId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 身份
     */
    private String identity;
}
