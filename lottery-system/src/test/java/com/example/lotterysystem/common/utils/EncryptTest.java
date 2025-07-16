package com.example.lotterysystem.common.utils;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.symmetric.AES;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;

@SpringBootTest
public class EncryptTest {
    @Test
    void sha256Test() {
        //哈希加密
        String encrypt = DigestUtil.sha256Hex("123456789");
        System.out.println("经历 shs256 哈希后的内容: " + encrypt);
        //15e2b0d3c33891ebb0f1ef609ec419420c20e320ce94c65fbc8c3312448eb225
    }

    /**
     * aes算法加密解密测试
     */
    @Test
    void aesTest() {
        // 原始密钥
        String rawKey = "123456789hgf";
        // 扩展密钥长度到 16 字节
        String key = rawKey + "0000000000000000"; // 填充到 16 字节
        System.out.println(key.substring(0,16));
        key = key.substring(0, 16); // 截取前 16 字节

        byte[] KEYS = key.getBytes(StandardCharsets.UTF_8);

        //根据Keys创建的aes对象
        AES aes = SecureUtil.aes(KEYS);
        //加密后的字符串
        String encrypt = aes.encryptHex("123456789");
        System.out.println("经过 aes 加密后的内容" + encrypt);

        //解密

        System.out.println("经过 aes 解密的内容" + SecureUtil.aes(KEYS).decryptStr(encrypt));

        //经过 aes 加密后的内容203c2b092d951e82fafcf0e932c4545f
        //经过 aes 解密的内容123456789
    }
}
