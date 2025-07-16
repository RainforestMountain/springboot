package com.example.lotterysystem.common.utils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CaptchaUtilTest {


    @Test
    void generateCaptchaCode() {
        System.out.println(CaptchaUtil.generateCaptchaCode(6));
    }
}