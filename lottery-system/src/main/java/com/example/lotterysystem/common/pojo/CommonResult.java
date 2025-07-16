package com.example.lotterysystem.common.pojo;

import cn.hutool.core.lang.Assert;
import com.example.lotterysystem.common.errorcode.ErrorCode;
import com.example.lotterysystem.common.errorcode.GlobalErrorCodeConstants;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * CommonResult<T> 作为控制器层⽅法的返回类型，封装 HTTP 接⼝调⽤的结果，包括成功数据、错
 * 误信息和状态码。它可以被 Spring Boot 框架等⾃动转换为 JSON 或其他格式的响应体，发送给客⼾
 * 端
 * 主要是业务错误码, 出现异常, 服务器捕获后, 也会返回错误码
 * <p>
 * 为什么要封装？
 * 1. 统⼀的返回格式：确保客⼾端收到的响应具有⼀致的结构，⽆论处理的是哪个业务逻辑。
 * 2. 错误码和消息：提供错误码（code）和错误消息（msg），帮助客⼾端快速识别和处理错误。
 * 3. 泛型数据返回：使⽤泛型 <T> 允许返回任何类型的数据，增加了返回对象的灵活性。
 * 4. 静态⽅法：提供了 error() 和 success() 静态⽅法，⽅便快速创建错误或成功的响应对象。
 * 5. 错误码常量集成：通过 ErrorCode 和 GlobalErrorCodeConstants 使⽤预定义的错误码，保持错
 * 误码的⼀致性和可维护性。
 * 6. 序列化：实现了 Serializable 接⼝，使得 CommonResult<T> 对象可以被序列化为多种格式，如
 * JSON或XML，⽅便⽹络传输。
 * 7. 业务逻辑解耦：将业务逻辑与API的响应格式分离，使得后端开发者可以专注于业务逻辑的实
 * 现，⽽不必关⼼如何构建HTTP响应。 8. 客⼾端友好：客⼾端开发者可以通过统⼀的接⼝获取数据和错误信息，
 * ⽆需针对每个API编写特定的错误处理逻辑。
 *
 * @param <T> 泛型, 类型参数化,可以传入一个具体的类型
 */

/**
 * 通用返回结果类，用于封装API接口的统一返回格式。
 * 包含错误码、返回数据和错误提示信息，支持泛型以适应不同类型的返回数据。
 *
 * @param <T> 返回数据的类型
 * @see ErrorCode 错误码定义类，包含业务错误码的定义
 * @see GlobalErrorCodeConstants 全局错误码常量接口
 */
@Data
public class CommonResult<T> implements Serializable {
    /**
     * 错误码
     * 0表示成功，非0表示失败。
     * 可参考ErrorCode类的code字段，或GlobalErrorCodeConstants接口中定义的常量。
     *
     * @see ErrorCode#getCode() 获取错误码的方法
     */
    private Integer code;

    /**
     * 返回数据
     * 当业务处理成功时，通常会携带具体的返回数据；
     * 当业务处理失败时，此值可能为null。
     */
    private T data;

    /**
     * 错误提示, 用户可以阅读
     * 通常用于前端展示给用户的错误描述，需保证语义清晰。
     *
     * @See ErrorCode#getMsg()
     */
    private String msg;


    /**
     * 将传入的result对象, 转换为另外一个泛型结果的对象
     * 主要用于统一错误码格式，例如将其他服务返回的错误结果转换为本服务的格式。
     *
     * @param result 传入的result对象 通常是其他服务返回的CommonResult<?>类型
     * @param <T>    返回的泛型
     * @return 新的CommonResult对象, 保持原错误码和错误信息，但泛型类型更新为T
     */
    public static <T> CommonResult<T> error(CommonResult<?> result) {
        return error(result.getCode(), result.getMsg());
    }

    /**
     * 重载的错误创建方法，通过错误码和错误信息创建结果对象
     *
     * @param code    错误码，必须是错误的（非200）
     * @param message 错误提示信息
     * @param <T>     返回的泛型类型
     * @return 新的CommonResult对象，包含指定的错误码和错误信息
     * @throws IllegalArgumentException 当code为200（成功码）时抛出异常
     */
    public static <T> CommonResult<T> error(Integer code, String message) {
        Assert.isTrue(!GlobalErrorCodeConstants.SUCCESS.getCode().equals(code), "code必须是错误的");
        CommonResult<T> result = new CommonResult<>();
        result.code = code;
        result.msg = message;
        return result;
    }

    /**
     * 通过ErrorCode接口创建错误结果对象
     *
     * @param errorCode 实现了ErrorCode接口的错误码对象
     * @param <T>       返回的泛型类型
     * @return 新的CommonResult对象，包含指定的错误码和错误信息
     */
    public static <T> CommonResult<T> error(ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMsg());
    }

    /**
     * 创建成功的结果对象，携带返回数据
     *
     * @param data 返回的数据
     * @param <T>  返回数据的类型
     * @return 新的CommonResult对象，错误码为200，携带指定数据
     */
    public static <T> CommonResult<T> success(T data) {
        CommonResult<T> result = new CommonResult<>();
        result.code = GlobalErrorCodeConstants.SUCCESS.getCode();
        result.data = data;
        result.msg = "";
        return result;
    }

    /**
     * 判断给定的错误码是否表示成功
     *
     * @param code 待判断的错误码
     * @return true表示成功，false表示失败
     */
    public static boolean isSuccess(Integer code) {
        return Objects.equals(code, GlobalErrorCodeConstants.SUCCESS.getCode());
    }

}
