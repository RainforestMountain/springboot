package com.example.lotterysystem.common.exception;

import com.example.lotterysystem.common.errorcode.ErrorCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * controller层自定义异常类, 认为是运行时异常
 */

/**
 * 该注解属于一个组合注解，其内部包含了@Getter、@Setter、@ToString、
 * &#064;EqualsAndHashCode以及@RequiredArgsConstructor这些注解。
 * 借助该注解，能够自动生成以下内容：
 * 所有非静态（non-static）和非 final 字段的 getter 方法。
 * 所有非 final 字段的 setter 方法。
 * 重写后的toString()方法，会输出类名以及所有字段。
 * 重写后的equals()和hashCode()方法，会基于对象的字段来进行比较和计算哈希值。
 * 针对所有带有@NonNull注解的字段或者 final 字段，会生成对应的构造函数。
 */
@Data
/**
 * @EqualsAndHashCode(callSuper = true) 注解
 * 功能
 * 此注解的作用是自动生成equals()和hashCode()方法。
 * 当callSuper = true时，会在生成的方法中包含父类的字段，
 * 也就是调用父类的equals()和hashCode()方法。这在处理继承关系时非常关键。
 * 要是不设置callSuper = true，那么生成的方法只会考虑当前类的字段，
 * 可能会引发与父类相关的比较问题
 * 父类字段参与比较
 * 避免循环引用问题：在存在双向关联的情况下
 * （例如，User类引用了Order类，而Order类又引用了User类），需
 * 要使用@EqualsAndHashCode.Exclude来排除可能引发循环的字段。
 */
@EqualsAndHashCode(callSuper = true)
public class ControllerException extends RuntimeException {
    /**
     * 主要业务异常码(错误码)
     *
     * @See com.example.lottery_system.common.errorcode.ControllerCodeConstants
     */
    private Integer code;
    /**
     * 错误提示
     */
    private String message;

    public ControllerException() {

    }

    public ControllerException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMsg();
    }

    public ControllerException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
