package com.example.lotterysystem.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.common.utils.JWTUtil;
import com.example.lotterysystem.common.utils.RegexUtil;
import com.example.lotterysystem.controller.param.ShortMessageLoginParam;
import com.example.lotterysystem.controller.param.UserLoginParam;
import com.example.lotterysystem.controller.param.UserPasswordLoginParam;
import com.example.lotterysystem.controller.param.UserRegisterParam;
import com.example.lotterysystem.dao.dataobject.Encrypt;
import com.example.lotterysystem.dao.dataobject.UserDO;
import com.example.lotterysystem.dao.mapper.UserMapper;
import com.example.lotterysystem.service.UserService;
import com.example.lotterysystem.service.VerificationCodeService;
import com.example.lotterysystem.service.dto.UserDTO;
import com.example.lotterysystem.service.dto.UserLoginDTO;
import com.example.lotterysystem.service.dto.UserRegisterDTO;
import com.example.lotterysystem.service.enums.UserIdentityEnum;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
//    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    //还需要dao层数据库的功能, mapper接口等等
    @Resource
    private UserMapper userMapper;

    //@Qualifier("verificationCodeService")
    @Autowired
    private VerificationCodeService verificationCodeService;

    @Override
    public UserRegisterDTO register(UserRegisterParam request) {
        //校验参数, 不符合就抛出异常, 然后用拦截器统一处理异常
        checkRegisterInfo(request);
        //入库,
        UserDO userDo = new UserDO();
        //设置属性
        userDo.setUserName(request.getName());
        userDo.setIdentity(request.getIdentity());
        userDo.setEmail(request.getMail());
        if (StringUtils.hasText(request.getPassword())) {
            //对密码进行哈希加密,一般是加盐哈希
            userDo.setPassword(DigestUtil.sha256Hex(request.getPassword()));
        }
        userDo.setPhoneNumber(new Encrypt(request.getPhoneNumber()));
        userMapper.insert(userDo);
        //返回DTO, DTO在service层传递
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setUserId(userDo.getId());
        return userRegisterDTO;
    }

    @Override
    public UserLoginDTO login(UserLoginParam request) {
        UserLoginDTO userLoginDTO = null;
        //针对param的子类类型进行判断, 同时进行转换, 转换为子类类型
        if (request instanceof UserPasswordLoginParam loginParam) {
            //密码登录的话
            userLoginDTO = loginByPassword(loginParam);
        } else if (request instanceof ShortMessageLoginParam loginParam) {
            userLoginDTO = loginByShortMessage(loginParam);
        } else {
            //这个时候在服务层, 抛出服务层的错误
            throw new ServiceException(ServiceErrorCodeConstants.PASSWORD_ERROR);
        }
        return userLoginDTO;
    }

    /**
     * 查找人员列表的具体实现
     *
     * @param identity 身份,类型是枚举类型, 为null, 查找所有
     * @return
     */
    @Override
    public List<UserDTO> findUserList(UserIdentityEnum identity) {
        String identityString = (null == identity ? null : identity.name());
        List<UserDO> userDoList = userMapper.selectUserList(identityString);
        //要返回一个list,继续使用stream流和lambda表达式
        return userDoList.stream()
                .map(userDO -> {
                    UserDTO userDTO = new UserDTO();
                    userDTO.setUserId(userDO.getId());
                    userDTO.setUserName(userDO.getUserName());
                    userDTO.setEmail(userDO.getEmail());
                    userDTO.setPhoneNumber(userDO.getPhoneNumber().getValue());
                    userDTO.setIdentity(UserIdentityEnum.forName(userDO.getIdentity()));
                    return userDTO;
                }).collect(Collectors.toList());
    }

    /**
     * 验证密码登录的密码的正确性, 密码已经加盐哈希了
     *
     * @param loginParam
     * @return
     */
    private UserLoginDTO loginByShortMessage(ShortMessageLoginParam loginParam) {
        String loginMobile = loginParam.getLoginMobile();
        String verificationCode = loginParam.getVerificationCode();
        if (!RegexUtil.checkMobile(loginMobile)) {
            //手机号格式不对
            throw new ServiceException(ServiceErrorCodeConstants.PHONE_NUMBER_ERROR);
        }
        //用户查询, 通过手机号
        UserDO userDo = userMapper.selectByPhoneNumber(new Encrypt(loginMobile));
        if (null == userDo) {
            //查询到的用户为空
            throw new ServiceException(ServiceErrorCodeConstants.USER_NOT_EXIST);
        } else if (StringUtils.hasText(loginParam.getMandatoryIdentity()) && !loginParam.getMandatoryIdentity().equals(userDo.getIdentity())) {
            //存在强制身份登录, 但身份不相同, 说明是跨级别登录
            throw new ServiceException(ServiceErrorCodeConstants.IDENTITY_ERROR);

        }
        //验证码校验
        String code = verificationCodeService.getVerificationCode(loginMobile);
        if (!StringUtils.hasText(code) || !code.equals(verificationCode)) {
            //验证码要么为空, 要么就是不相等
            throw new ServiceException(ServiceErrorCodeConstants.VERIFICATION_CODE_ERROR);
        }
        //返回token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userDo.getId());//自定义内容，通常包含用户信息或其他需要存储在 JWT 中的数据
        claims.put("identity", userDo.getIdentity());
        String token = JWTUtil.genJWT(claims);
        //设置服务层的返回结果的属性的属性值
        UserLoginDTO userLoginDTO = new UserLoginDTO();
        userLoginDTO.setToken(token);
        userLoginDTO.setIdentity(UserIdentityEnum.forName(userDo.getIdentity()));
        return userLoginDTO;
    }

    private UserLoginDTO loginByPassword(UserPasswordLoginParam loginParam) {
        String loginName = loginParam.getLoginName();
        String password = loginParam.getPassword();
        //传给数据持久层的参数对象
        UserDO userDo = null;
        if (!StringUtils.hasText(loginName)) {
            throw new ServiceException(ServiceErrorCodeConstants.LOGIN_NAME_IS_EMPTY);
        }
        if (!StringUtils.hasText(password)) {
            throw new ServiceException(ServiceErrorCodeConstants.PASSWORD_ERROR);
        }
        //获取用户信息,从而验证密码是否正确
        //同时验证邮箱,手机号是否符合格式
        if (RegexUtil.checkMail(loginName)) {
            //根据邮箱获取用户信息
            userDo = userMapper.selectByEmail(loginName);
        } else if (RegexUtil.checkMobile(loginName)) {
            userDo = userMapper.selectByPhoneNumber(new Encrypt(loginName));
        } else {
            throw new ServiceException(ServiceErrorCodeConstants.MAIL_OR_PHONE_ERROR);
        }
        if (null == userDo) {
            throw new ServiceException(ServiceErrorCodeConstants.USER_NOT_EXIST);
        } else if (StringUtils.hasText(loginParam.getMandatoryIdentity()) && !loginParam.getMandatoryIdentity().equals(userDo.getIdentity())) {
            //存在强制身份登录, 但是身份不同
            throw new ServiceException(ServiceErrorCodeConstants.IDENTITY_ERROR);
        } else if (!userDo.getPassword().equals(DigestUtil.sha256Hex(password))) {
            //数据库获取的密码字符串与登录密码哈希的字符串比对
            throw new ServiceException(ServiceErrorCodeConstants.PASSWORD_ERROR);
        }
        //获取到用户信息并且密码正确, 生成jet令牌, 返回token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userDo.getPassword());
        claims.put("identity", userDo.getIdentity());
        String token = JWTUtil.genJWT(claims);

        UserLoginDTO userLoginDTO = new UserLoginDTO();
        userLoginDTO.setToken(token);
        userLoginDTO.setIdentity(UserIdentityEnum.forName(userDo.getIdentity()));
        return userLoginDTO;
    }


    /**
     * 校验参数是否符合注册要求
     *
     * @param request
     */
    private void checkRegisterInfo(UserRegisterParam request) {
        if (null == request) {
            throw new ServiceException(ServiceErrorCodeConstants.REGISTER_INFO_IS_EMPTY);
        }
        //邮箱格式校验
        if (!RegexUtil.checkMail(request.getMail())) {
            throw new ServiceException(ServiceErrorCodeConstants.MAIL_ERROR);
        }
        //手机格式校验
        if (!RegexUtil.checkMobile(request.getPhoneNumber())) {
            throw new ServiceException(ServiceErrorCodeConstants.PHONE_NUMBER_ERROR);
        }
        //检查身份信息
        if (null == UserIdentityEnum.forName(request.getIdentity())) {
            throw new ServiceException(ServiceErrorCodeConstants.IDENTITY_ERROR);
        }
        //管理员必须设置密码
        if (request.getIdentity().equals(UserIdentityEnum.ADMIN.name()) && !StringUtils.hasText(request.getPassword())) {
            throw new ServiceException(ServiceErrorCodeConstants.PASSWORD_ERROR);
        }

        //密码格式校验, 最少6位
        if (StringUtils.hasText(request.getPassword()) && !RegexUtil.checkPassword(request.getPassword())) {
            throw new ServiceException(ServiceErrorCodeConstants.PASSWORD_FORMAT_ERROR);
        }
        //检查邮箱是否被使用
        if (checkMailUsed(request.getMail())) {
            throw new ServiceException(ServiceErrorCodeConstants.MAIL_USED);
        }
        //检查手机号是否被使用
        if (checkPhoneUsed(request.getPhoneNumber())) {
            throw new ServiceException(ServiceErrorCodeConstants.PHONE_NUMBER_USED);
        }
    }

    /**
     * 检查手机号是否被使用
     *
     * @param phoneNumber
     * @return
     */
    private boolean checkPhoneUsed(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            throw new ServiceException(ServiceErrorCodeConstants.PHONE_IS_EMPTY);
        }
        //根据手机号统计使用者
        int countUser = userMapper.countByPhoneNumber(new Encrypt(phoneNumber));

        return countUser > 0;//这个手机号使用者是否大于0
    }

    /**
     * 检查邮箱是否被使用
     *
     * @param mail
     * @return
     */
    private boolean checkMailUsed(String mail) {
        if (!StringUtils.hasText(mail)) {
            throw new ServiceException(ServiceErrorCodeConstants.MAIL_IS_EMPTY);
        }
        //邮箱被多少人使用
        int countUser = userMapper.countByMail(mail);
        return countUser > 0;
    }
}
