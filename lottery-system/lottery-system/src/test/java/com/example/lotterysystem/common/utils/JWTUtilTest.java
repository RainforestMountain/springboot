package com.example.lotterysystem.common.utils;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class JWTUtilTest {
    /**
     * 生成一个 Base64 编码的密钥
     * SecureRandom：
     * SecureRandom 是 Java 提供的一个安全的随机数生成器，用于生成高质量的随机数。
     * 它比普通的 Random 类更安全，适合用于生成密钥等需要高安全性的场景。
     * keySize：
     * keySize 参数指定生成的密钥的大小（以字节为单位）。
     * 例如，32 字节对应 256 位密钥，这是常见的 AES 加密密钥大小。
     * random.nextBytes(keyBytes)：
     * 使用 SecureRandom 生成指定大小的随机字节数组。
     * Base64.getEncoder().encodeToString(keyBytes)：
     * 将生成的字节数组转换为 Base64 编码的字符串。
     *
     * @return Base64 编码的密钥
     */
    @Test
    public void generateBase64Key() {
        int keySize = 32;
        // 创建一个安全的随机数生成器
        SecureRandom random = new SecureRandom();
        // 生成指定大小的字节数组
        byte[] keyBytes = new byte[keySize];
        random.nextBytes(keyBytes);
        // 将字节数组转换为 Base64 编码的字符串
        System.out.println(Base64.getEncoder().encodeToString(keyBytes));
    }

    @Test
    void genJWT() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("key1", "value1");
        claims.put("key2", "value2");
        String jwt = JWTUtil.genJWT(claims);
        System.out.println(jwt);

        Claims claims1 = JWTUtil.parseJWT(jwt);
        for (Map.Entry<String, Object> entry : claims1.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }


    }
}