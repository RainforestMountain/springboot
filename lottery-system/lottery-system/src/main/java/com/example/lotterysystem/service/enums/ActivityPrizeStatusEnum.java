package com.example.lotterysystem.service.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ActivityPrizeStatusEnum {

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

    public static ActivityPrizeStatusEnum forName(String name) {
        for (ActivityPrizeStatusEnum activityPrizeStatusEnum : ActivityPrizeStatusEnum.values()) {
            if (activityPrizeStatusEnum.name().equalsIgnoreCase(name)) {
                return activityPrizeStatusEnum;
            }
        }
        throw new IllegalArgumentException("Invalid name: " + name);
    }
}
