package com.example.lotterysystem.controller.result;

import lombok.Data;

import java.io.Serializable;

//用户登录时, 登录接口的返回结果
@Data
public class UserLoginResult implements Serializable {
    /**
     * JWT 令牌
     */
    private String token;
    /**
     * 身份
     */
    private String identity;
}
