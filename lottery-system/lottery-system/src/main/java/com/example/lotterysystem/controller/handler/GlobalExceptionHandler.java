package com.example.lotterysystem.controller.handler;

import com.example.lotterysystem.common.errorcode.GlobalErrorCodeConstants;
import com.example.lotterysystem.common.exception.ControllerException;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.common.pojo.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 控制层通⽤异常处理 @RestControllerAdvice+@ExceptionHandler
 * spring boot中使⽤ @RestControllerAdvice 注解，完成优雅的全局异常处理类，可以针对所有异常类
 * 型先进⾏通⽤处理后，再对特定异常类型进⾏不同的处理操作
 */

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    //具体处理什么异常

    /**
     * 全局异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    public CommonResult<?> exceptionHandler(Exception e) {
        logger.error("服务错误: ", e);
        //返回系统异常的统一返回结果, ErrorCode INTERNAL_SERVER_ERROR = new ErrorCode(500, "系统异常");
        return CommonResult.error(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                e.getMessage());
    }

    /**
     * 处理自定义服务层的异常
     * 抛出的服务层异常, 有对应的错误信息, 然而错误代码是全局系统错误代码500,
     * 信息呢,是具体服务层异常的信息
     *
     * @param serviceException
     * @return
     */
    @ExceptionHandler(value = ServiceException.class)
    public CommonResult<?> exceptionHandler(ServiceException serviceException) {
        logger.error("serviceExceptionHandler: " + serviceException);
        return CommonResult.error(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                serviceException.getMessage());
    }

    /**
     * 控制层的异常
     * @param controllerException
     * @return
     */
    @ExceptionHandler(value = ControllerException.class)
    public CommonResult<?> exceptionHandler(ControllerException controllerException) {
        //打印错误级别的日志
        logger.error("controllerExceptionHandler: " + controllerException);
        return CommonResult.error(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                controllerException.getMessage());
    }

}
