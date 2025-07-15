package com.example.lotterysystem.service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 在 Spring Boot 服务层中，使用枚举类（enum）
 * 表示身份（如角色、权限级别等）相比使用字符串有以下显著优势：
 * 1. 类型安全
 * 枚举类提供编译时类型检查，确保只能使用预定义的身份值。
 * 而字符串可能因拼写错误或随意传入非法值导致运行时异常。
 * 2. 代码可读性与可维护性
 * 枚举值具有明确的语义，代码更易理解。
 * 当需要修改或扩展身份类型时，只需在枚举类中调整，无需全局搜索替换字符串。
 * 3.避免魔法字符串
 * 字符串常量可能在代码中分散出现，导致维护困难。枚举类将所有身份值集中管理，便于统一修改。
 * 4. 可扩展性
 * 枚举类可以添加方法和属性，扩展身份的行为或元数据。
 */
@Getter
@AllArgsConstructor
public enum UserIdentityEnum {
    NORMAL("普通用户"),
    ADMIN("管理员");

    private final String message;

    /**
     * 通过名称获取枚举对象
     *
     * @param name
     * @return
     */
    public static UserIdentityEnum forName(String name) {
        for (UserIdentityEnum userIdentityEnum : UserIdentityEnum.values()) {
            if (userIdentityEnum.name().equalsIgnoreCase(name)) {
                return userIdentityEnum;
            }
        }
        return null;
    }
}
