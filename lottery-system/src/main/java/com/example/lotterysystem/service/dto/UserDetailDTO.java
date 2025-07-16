package com.example.lotterysystem.service.dto;

import com.example.lotterysystem.service.enums.ActivityUserStatusEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserDetailDTO implements Serializable {
    /**
     * 用户id
     */
    private Long userId;

    /**
     * 姓名
     */
    private String userName;

    /**
     * 状态
     */
    private ActivityUserStatusEnum status;

    public Boolean valid() {
        return status.equals(ActivityUserStatusEnum.INIT);
    }
}
