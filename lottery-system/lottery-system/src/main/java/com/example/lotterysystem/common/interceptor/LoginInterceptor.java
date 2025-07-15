package com.example.lotterysystem.common.interceptor;

import com.example.lotterysystem.common.utils.JWTUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从header中获取token
        String jwtToken = request.getHeader("user_token");
        log.info("获取路径: {}", request.getRequestURI());
        log.info("从header中获取token:{}", jwtToken);
        //jwt, 数字签名的验证
        //先解析令牌
        Claims claims = JWTUtil.parseJWT(jwtToken);
        if (null == claims) {
            response.setStatus(401);
            return false;
        }
        log.info("令牌验证通过, 放行");
        return true;
    }
}
