package com.example.lotterysystem.service;

import com.example.lotterysystem.controller.param.UserLoginParam;
import com.example.lotterysystem.controller.param.UserRegisterParam;
import com.example.lotterysystem.service.dto.UserDTO;
import com.example.lotterysystem.service.dto.UserLoginDTO;
import com.example.lotterysystem.service.dto.UserRegisterDTO;
import com.example.lotterysystem.service.enums.UserIdentityEnum;

import java.util.List;

/**
 * Service 层接⼝设计
 * 为什么进⾏接⼝分离设计？接⼝与实现的分离是 Java 编程中推崇的⼀种设计哲学，它有助于创建更加
 * 灵活、可维护和可扩展的软件系统
 */

public interface UserService {
    /**
     * 用户注册的业务接口
     *
     * @param param
     * @return
     */
    UserRegisterDTO register(UserRegisterParam param);

    /**
     * 用户登录
     * 1.邮箱/手机号 + 密码
     * 2.手机号 + 验证码
     *
     * @param request
     * @return
     */
    UserLoginDTO login(UserLoginParam request);

    /**
     * 获取用户列表
     *
     * @param identity 身份,类型是枚举类型, 为null, 查找所有
     * @return
     */
    List<UserDTO> findUserList(UserIdentityEnum identity);
}
