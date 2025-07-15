package com.example.lotterysystem.controller;


import com.example.lotterysystem.common.errorcode.ControllerCodeConstants;
import com.example.lotterysystem.common.exception.ControllerException;
import com.example.lotterysystem.common.pojo.CommonResult;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.controller.param.ShortMessageLoginParam;
import com.example.lotterysystem.controller.param.UserPasswordLoginParam;
import com.example.lotterysystem.controller.param.UserRegisterParam;
import com.example.lotterysystem.controller.result.UserBaseInfoResult;
import com.example.lotterysystem.controller.result.UserLoginResult;
import com.example.lotterysystem.controller.result.UserRegisterResult;
import com.example.lotterysystem.service.UserService;
import com.example.lotterysystem.service.VerificationCodeService;
import com.example.lotterysystem.service.dto.UserDTO;
import com.example.lotterysystem.service.dto.UserLoginDTO;
import com.example.lotterysystem.service.dto.UserRegisterDTO;
import com.example.lotterysystem.service.enums.UserIdentityEnum;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@RestController
@CrossOrigin
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private VerificationCodeService verificationCodeService;

    /**
     * 用户注册
     * Validation
     * 对于 controller 接⼝⼊参字段的验证，可以使⽤ Spring Boot 中集成的 Validation 来完成。例如可以
     * 看到我们在接⼝⼊参上加⼊了 @Validated 注解，并且 param 对象中的每个成员都使⽤
     *
     * @param param
     * @return
     * @Validated 是 Spring 提供的一个注解，用于触发数据校验。它通常与 @Valid 注解一起使用，但 @Validated 提供了更灵活的校验功能。
     * 作用
     * 触发数据校验：当方法参数被 @Validated 注解标记时，Spring 会自动对参数进行数据校验。
     * 比如 @NotBlank(message = "用户姓名不能为空! ")
     * 支持分组校验：@Validated 支持分组校验，可以通过指定不同的分组来对不同的字段进行校验。
     * @NotBlank 注解来检查参数不能为空。使⽤需引⼊依赖
     * @RequestBody 是 Spring MVC 提供的一个注解，用于将 HTTP 请求的正文（body）映射到方法参数中。它通常用于处理 POST 或 PUT 请求，这些请求通常包含 JSON 或 XML 格式的请求体。
     * 作用
     * 请求体映射：将 HTTP 请求的正文映射到方法参数中，通常用于接收 JSON 或 XML 格式的请求体。
     * 自动反序列化：Spring 会自动将请求体反序列化为指定的 Java 对象。
     */
    @RequestMapping("/register")
    public CommonResult<UserRegisterResult> userRegister(@Validated @RequestBody UserRegisterParam param) {
        logger.info("userRegister UserRegisterParam :" + JacksonUtil.writeValueAsString(param));
        UserRegisterDTO userRegisterDTO = userService.register(param);
        return CommonResult.success(convertToRegisterResult(userRegisterDTO));
    }

    /**
     * 根据电话号码发送验证码
     *
     * @param phoneNumber
     * @return
     */
    @RequestMapping("/verification-code/send")
    public CommonResult<Boolean> sendVerificationCode(String phoneNumber) {
        //info级别的日志
        logger.info("sendVerificationCode phoneNumber : " + phoneNumber);
        verificationCodeService.sendVerificationCode(phoneNumber);
        //发送成功, 封装成统一的成功返回结果
        return CommonResult.success(Boolean.TRUE);
    }

    @RequestMapping("/password/login")
    public CommonResult<UserLoginResult> userLogin(@Validated @RequestBody UserPasswordLoginParam param) {
        //参数序列化为json字符串
        logger.info("userLogin UserPasswordLoginParam" + JacksonUtil.writeValueAsString(param));
        UserLoginDTO userLoginDTO = userService.login(param);
        //封装成统一成功返回结果
        return CommonResult.success(convertToLoginResult(userLoginDTO));
    }

    @RequestMapping("/message/login")
    public CommonResult<UserLoginResult> shortMessageLogin(@Validated @RequestBody ShortMessageLoginParam param) {
        logger.info("shortMessageLogin ShortMessageLoginParam: " + JacksonUtil.writeValueAsString(param));
        UserLoginDTO userLoginDTO = userService.login(param);
        return CommonResult.success(convertToLoginResult(userLoginDTO));
    }

    /**
     * 查找人员信息列表的接口
     * 前端传递的参数不是一个自定义对象, 而是String
     *
     * @param identity
     * @return
     */
    @RequestMapping("/base-user/find-list")
    public CommonResult<List<UserBaseInfoResult>> findUserBaseInfoList(String identity) {
        logger.info("findUserList");
        List<UserDTO> userDTOList = null;
        if (!StringUtils.hasText(identity)) {
            //身份是空字符串, 或者是空
            userDTOList = userService.findUserList(null);
        } else if (null != UserIdentityEnum.forName(identity)) {
            userDTOList = userService.findUserList(UserIdentityEnum.forName(identity));
        }
        return CommonResult.success(convertToFindUserListResult(userDTOList));
    }

    /**
     * 人员列表,控制层和服务层返回结果的转换
     *
     * @param userDTOList
     * @return
     */
    private List<UserBaseInfoResult> convertToFindUserListResult(List<UserDTO> userDTOList) {
        if (CollectionUtils.isEmpty(userDTOList)) {
            return Arrays.asList();//服务层的DTOList为空, 转换为空列表
        }
        //用stream流来转换, 链式方法
        //里面有一个lambda代码块, 具体转换由lambda实现
        return userDTOList.stream()
                .map(userDTO -> { // 中间操作：对每个 UserDTO 元素执行转换操作, 参数是userDTO
                    // 创建目标对象
                    UserBaseInfoResult userBaseInfo = new UserBaseInfoResult();

                    // 复制基本属性
                    userBaseInfo.setUserId(userDTO.getUserId());
                    userBaseInfo.setUserName(userDTO.getUserName());
                    // 将枚举类型的 identity 转换为字符串（使用枚举的 name() 方法）
                    userBaseInfo.setIdentity(userDTO.getIdentity().name());

                    // 返回转换后的对象
                    return userBaseInfo;
                }).collect(Collectors.toList());// 终端操作：将 Stream 中的元素收集到一个 List 中
    }


    /**
     * 重载方法
     * 转换服务层的返回的结果为控制层的返回结果, 最后封装成统一返回结果
     *
     * @param userRegisterDTO
     * @return
     */
    private UserRegisterResult convertToRegisterResult(UserRegisterDTO userRegisterDTO) {
        if (null == userRegisterDTO) {
            //在控制层, 抛出控制层的异常, 转换后返回结果是空, 注册错误
            throw new ControllerException(ControllerCodeConstants.REGISTER_ERROR);
        }
        UserRegisterResult result = new UserRegisterResult();
        result.setUserId(userRegisterDTO.getUserId());
        return result;
    }

    private UserLoginResult convertToLoginResult(UserLoginDTO userLoginDTO) {
        if (null == userLoginDTO) {
            throw new ControllerException(ControllerCodeConstants.LOGIN_BY_PASSWORD_ERROR);
        }
        UserLoginResult result = new UserLoginResult();
        result.setToken(userLoginDTO.getToken());
        result.setIdentity(userLoginDTO.getIdentity().name());
        return result;
    }

}
