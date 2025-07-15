package com.example.lotterysystem.controller.result;

import lombok.Data;

import java.io.Serializable;

//用户注册时, 注册接口返回的结果
@Data
public class UserRegisterResult implements Serializable {
    /**
     * 用户id
     */
    private Long userId;
}
