package com.example.lotterysystem.common.utils;


import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import org.springframework.stereotype.Component;

@Component
public class CaptchaUtil {
    /**
     * 生成多少位的数字验证码
     * 用hutool工具包的
     *
     * @param length
     * @return
     */
    public static String generateCaptchaCode(int length) {
        RandomGenerator randomGenerator = new RandomGenerator("0123456789", length);
        LineCaptcha lineCaptha = cn.hutool.captcha.CaptchaUtil.createLineCaptcha(200, 100);
        lineCaptha.setGenerator(randomGenerator);
        //重新生成code
        lineCaptha.createCode();
        return lineCaptha.getCode();
    }
}
