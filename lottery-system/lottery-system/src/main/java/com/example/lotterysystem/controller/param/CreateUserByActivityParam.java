package com.example.lotterysystem.controller.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 活动关联的人员信息参数
 */
@Data
public class CreateUserByActivityParam {
    /**
     * 用户id
     */
    @NotNull(message = "用户id不为空")
    private Long userId;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String userName;
}
