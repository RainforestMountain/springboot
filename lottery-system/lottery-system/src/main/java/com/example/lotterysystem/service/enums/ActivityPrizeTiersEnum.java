package com.example.lotterysystem.service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 枚举类（enum）是一种特殊的类，用于表示固定数量的常量值。枚举类的属性设计需遵循特定规则和最佳实践，以下是详细说明：
 * 1. 基本属性定义规则
 * 静态常量：枚举值本质是类的静态实例，不可变。
 * 私有属性：属性通常声明为 private final，通过构造器初始化。
 * 构造器：构造器必须为 private（隐式或显式），防止外部实例化。
 * name() 是 枚举常量的常量名称, FIRST_PRIZE.name() 是FIRST_PRIZE
 * getMessage() 是枚举常量的属性值, FIRST_PRIZE.getMessage() 是一等奖
 */
@AllArgsConstructor
@Getter
public enum ActivityPrizeTiersEnum {
    FIRST_PRIZE(1, "一等奖"),
    SECOND_PRIZE(2, "二等奖"),
    THIRD_PRIZE(3, "三等奖");

    /**
     * code, 具体代码编号
     */
    private final Integer code;
    /**
     * 这个枚举常量的信息
     */
    private final String message;

    //通过枚举常量的名字获取枚举常量
    public static ActivityPrizeTiersEnum forName(String name) {
        for (ActivityPrizeTiersEnum activityPrizeTiersEnum : ActivityPrizeTiersEnum.values()) {
            if (activityPrizeTiersEnum.name().equalsIgnoreCase(name)) {
                //忽略大小写
                return activityPrizeTiersEnum;
            }
        }
        //不合法参数异常
        throw new IllegalArgumentException("Invalid name: " + name);
    }

    /**
     * 通过具体描述来获取枚举常量
     *
     * @param message
     * @return
     */
    public static ActivityPrizeTiersEnum fromMessage(String message) {
        for (ActivityPrizeTiersEnum activityPrizeTiersEnum : ActivityPrizeTiersEnum.values()) {
            if (activityPrizeTiersEnum.getMessage().equalsIgnoreCase(message)) {
                return activityPrizeTiersEnum;
            }
        }
        throw new IllegalArgumentException("Invalid message :" + message);
    }

}
