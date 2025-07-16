package com.example.lotterysystem.service.impl;

import com.example.lotterysystem.common.utils.RedisUtil;
import com.example.lotterysystem.common.utils.SMSUtil;
import com.example.lotterysystem.service.VerificationCodeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class VerificationCodeImpl implements VerificationCodeService {
    //前缀
    private static final String VERIFICATION_CODE_PREFIX = "verification_code_";
    //有效时间
    private static final Integer VERIFICATION_CODE_EFFECTIVE_TIME = 60;
    //template, 模板
    private static final String VERIFICATION_TEMPLATE_CODE = "SMS_465324787";

    @Resource
    private SMSUtil smsUtil;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public void sendVerificationCode(String phoneNumber) {

    }

    @Override
    public String getVerificationCode(String phoneNumber) {
        return "";
    }
}
