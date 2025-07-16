package com.example.lotterysystem.controller.param;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRegisterParam {
    /**
     * 用户姓名
     */

    @NotBlank(message = "用户姓名不能为空! ")
    private String name;

    /**
     * 用户邮箱
     */
    @NotBlank(message = "用户邮箱不能为空! ")
    private String mail;

    /**
     * 用户电话号码
     */
    @NotBlank(message = "用户电话号码不能为空! ")
    private String phoneNumber;

    /**
     * 登录密码
     * 普通用户不设置密码
     */
    private String password;

    /**
     * 身份
     */
    @NotBlank(message = "身份信息不能为空！")
    private String identity;
}
