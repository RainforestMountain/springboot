package com.example.lotterysystem.service.dto;

import com.example.lotterysystem.service.enums.UserIdentityEnum;
import lombok.Data;

/**
 * 服务层的返回结果
 */
@Data
public class UserLoginDTO {
    /**
     * JWT令牌
     */
    private String token;

    /**
     * 身份
     */
    private UserIdentityEnum identity;
}
