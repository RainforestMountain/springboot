package com.example.lotterysystem.service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ActivityUserStatusEnum {

    INIT(1, "初始化"),
    COMPLETED(2, "已经被抽取");
    /**
     * 状态码
     */
    private final Integer code;
    /**
     * 描述
     */
    private final String message;

    public static ActivityUserStatusEnum forName(String name) {
        for (ActivityUserStatusEnum activityUserStatusEnum : ActivityUserStatusEnum.values()) {
            if (activityUserStatusEnum.name().equalsIgnoreCase(name)) {
                return activityUserStatusEnum;
            }
        }
        throw new IllegalArgumentException("Invalid name: " + name);
    }
}
