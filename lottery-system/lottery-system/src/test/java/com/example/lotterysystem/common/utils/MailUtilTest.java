package com.example.lotterysystem.common.utils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MailUtilTest {
    @Autowired
    private MailUtil mailUtil;

    @Test
    void sendSampleMail() {
        mailUtil.sendSampleMail("3301896206@qq.com", "test", "mailUtil,test");
    }
}