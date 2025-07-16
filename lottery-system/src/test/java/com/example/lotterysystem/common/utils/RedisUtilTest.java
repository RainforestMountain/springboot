package com.example.lotterysystem.common.utils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RedisUtilTest {
    @Autowired
    private RedisUtil redisUtil;

    @Test
    void redisTest() {
//        redisUtil.set("num", String.valueOf(1));
//        redisUtil.set("num1", String.valueOf(2));
//        System.out.println(redisUtil.hasKey("num"));
//        System.out.println(redisUtil.get("num"));
//        redisUtil.set("num2", String.valueOf(32), 60);
//        redisUtil.set("num3", String.valueOf(4), -9);
//        System.out.println(redisUtil.get("num2"));
//        System.out.println(redisUtil.getExpire("num"));
//        System.out.println(redisUtil.getExpire("num2"));
//
//        redisUtil.setExpire("num2", 1000);
//        System.out.println(redisUtil.getExpire("num2"));

        redisUtil.del("num2");
    }
}