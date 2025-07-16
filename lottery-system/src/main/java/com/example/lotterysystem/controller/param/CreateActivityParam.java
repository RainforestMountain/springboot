package com.example.lotterysystem.controller.param;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建活动时,传递的参数
 */

/**
 * @NotBlank和@NotEmpty这两个注解都来自于 Jakarta Bean Validation（JSR 380）规范，它们的主要区别在于适用的数据类型和验证的严格程度。
 * 1. @NotBlank
 * 适用类型：字符串（String）。
 * 验证规则：
 * 字符串不为null。
 * 字符串的长度大于 0。
 * 字符串不能只包含空白字符（如空格、制表符、换行符等）。
 * 2. @NotEmpty
 * 适用类型：
 * 集合（如List、Set等）。
 * 数组。
 * 字符串（String）。
 * 验证规则：
 * 对象不为null。
 * 集合 / 数组的大小大于 0，或字符串的长度大于 0。
 * 允许字符串包含空白字符（只要长度大于 0）。
 * @NotEmpty通常用于验证集合或数组不为空， 但它不会验证集合中的元素是否有效。
 * 若需对集合内的每个元素进行嵌套验证，需配合以下注解：
 * 1. @Valid（最常用）
 * 作用：
 * <p>
 * 触发对集合中每个元素的递归验证（验证元素内部的注解）。
 * 适用于自定义对象类型的集合（如List<CreatePrizeActivityParam>）。
 * 3.@NotNull
 * 适用类型：所有类型（对象、基本类型包装类、字符串等）。
 * 验证规则：
 * 对象不为 null。
 * 允许空值（如空字符串 ""、空集合 []）。
 */
@Data
public class CreateActivityParam implements Serializable {
    /**
     * 活动名称
     */
    @NotBlank(message = "活动名称不能为空")
    private String activityName;

    /**
     * 活动描述
     */
    @NotBlank(message = "活动描述不能为空")
    private String description;

    /**
     * 活动关联的奖品信息
     */
    @NotEmpty(message = "活动关联的奖品信息不能为空")
    @Valid
    private List<CreatePrizeByActivityParam> activityPrizeList;

    /**
     * 活动关联的人员信息
     */
    @NotEmpty(message = "活动关联的人员信息不能为空")
    @Valid
    private List<CreateUserByActivityParam> activityUserList;

}
