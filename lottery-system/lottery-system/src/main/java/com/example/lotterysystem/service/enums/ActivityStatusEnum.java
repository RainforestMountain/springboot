package com.example.lotterysystem.service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 活动状态枚举类
 */
@AllArgsConstructor
@Getter
public enum ActivityStatusEnum {
    RUNNING(1, "正在进行"),
    COMPLETED(2, "已经完成");

    /**
     * 状态码
     */
    private final Integer code;
    /**
     * 描述
     */
    private final String message;

    /**
     * 根据枚举常量的字符串名称来获取枚举常量对象
     *
     * @return
     */
    public static ActivityStatusEnum forName(String name) {
        for (ActivityStatusEnum activityStatusEnum : ActivityStatusEnum.values()) {
            if (activityStatusEnum.name().equalsIgnoreCase(name)) {
                return activityStatusEnum;
            }
        }
        //没有对应的枚举常量对象, 那么说明name不对
        throw new IllegalArgumentException("Invalid name :" + name);
    }
}
