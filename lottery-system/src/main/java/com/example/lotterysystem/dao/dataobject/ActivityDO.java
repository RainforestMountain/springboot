package com.example.lotterysystem.dao.dataobject;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 1. @Data 注解的作用
 * 自动生成：
 * Getter/Setter：为所有非 final 字段生成 getter 方法，为所有字段生成 setter 方法。
 * equals () 和 hashCode ()：根据类的非静态、非 transient 字段生成。
 * toString()：生成包含所有字段的字符串表示形式。
 * 构造方法：生成一个包含所有 final 和非 @NonNull 字段的构造方法。
 * 其他：canEqual() 方法（用于子类比较）。
 * 2. @EqualsAndHashCode(callSuper = true) 的作用
 * 默认行为：
 * Lombok 生成的 equals() 和 hashCode() 方法默认不包含父类字段（仅比较子类定义的字段）。
 * 启用 callSuper = true：
 * 强制 equals() 和 hashCode() 方法包含父类的字段（调用父类的 equals() 和 hashCode()）。
 * 前提条件：父类必须正确实现 equals() 和 hashCode()（否则可能导致逻辑错误）。
 * 3.明确是否需要父类字段参与比较：
 * 若子类比较需包含父类状态（如 BaseDO 包含 id），使用 callSuper = true。
 * 若仅比较子类特有字段，可省略 callSuper（默认 false）。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ActivityDO extends BaseDO {
    /**
     * 活动名称
     */
    private String activityName;

    /**
     * 活动描述
     */
    private String description;

    /**
     * 活动状态
     *
     * @See ActivityStatusEnum#name()
     */
    private String status;
}
