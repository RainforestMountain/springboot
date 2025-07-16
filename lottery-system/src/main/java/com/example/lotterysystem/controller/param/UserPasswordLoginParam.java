package com.example.lotterysystem.controller.param;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

//密码登录时, 前端传递过来的参数形式,

@Data
@EqualsAndHashCode(callSuper = true)
public class UserPasswordLoginParam extends UserLoginParam {
    /**
     * 邮箱 或者手机号, 属性有效性验证
     */
    @NotBlank(message = "邮箱或者手机号不能为空")
    private String loginName;

    /**
     * 密码
     */
    @NotBlank(message = "登录密码不能为空")
    private String password;

}


