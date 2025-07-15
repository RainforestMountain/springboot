package com.example.lotterysystem.controller.param;

import com.example.lotterysystem.service.enums.UserIdentityEnum;
import lombok.Data;

//实现Serializable接口, 可以序列化
//登录时传递的参数, 是密码登录和验证码登录的父类
@Data
public class UserLoginParam {
    /**
     * 是否强制某身份才可以登录, 可以为空
     *
     * @see UserIdentityEnum#name()
     */
    private String mandatoryIdentity;
}
