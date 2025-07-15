package com.example.lotterysystem.common.utils;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RegexUtilTest {

    @Test
    void checkMail() {
        System.out.println(RegexUtil.checkMail("4515889777@qq.com"));
    }

    @Test
    void checkMobile() {
        System.out.println(RegexUtil.checkMobile("19815472389"));
    }

    @Test
    void checkPassword() {
        System.out.println(RegexUtil.checkPassword("jksoie6544"));
    }
}