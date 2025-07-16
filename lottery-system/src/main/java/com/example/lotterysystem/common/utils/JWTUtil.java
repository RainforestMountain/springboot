package com.example.lotterysystem.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 传统的⽤⼾登录思路：
 * • 登陆⻚⾯把⽤⼾名密码提交给服务器.
 * • 服务器端验证⽤⼾名密码是否正确, 并返回校验结果给后端
 * • 如果密码正确, 则在服务器端创建 Session . 通过 Cookie 把 sessionId 返回给浏览器.
 * 问题：集群环境下⽆法直接使⽤Session。服务器有多台, 导致session存储会出现 多台机器不一致问题
 * <p>
 * JWT 的组成部分
 * JWT 由三部分组成，每部分之间用点（.）分隔：
 * Header（头部）
 * Payload（负载）
 * Signature（签名）
 * 1. Header（头部）
 * 头部通常包含两部分：令牌的类型（即 JWT）和所使用的签名算法（如 HMAC SHA256 或 RSA）。
 * 2. Payload（负载） (这个类的claims就是负载部分,一般是键值对格式)
 * 负载包含声明（claims），这些声明是关于实体（通常是用户）和其他数据的声明。声明可以分为三种类型：
 * Registered Claims（注册声明）：预定义的声明，用于提供一组通用的声明，
 * 如 iss（发行人）、exp（过期时间）、sub（主题）等。
 * Public Claims（公共声明）：自定义的声明，用于存储特定于应用的信息。
 * Private Claims（私有声明）：自定义的声明，用于存储特定于应用的信息，通常不公开。
 * 3. Signature（签名）
 * 签名部分用于验证消息的内容是否在传输过程中被篡改。签名是通过对头部和负载进行 Base64 编码，
 * 然后使用指定的算法和密钥进行签名生成的。
 * 生成签名的步骤：
 * 将头部和负载进行 Base64 编码。
 * 将编码后的头部和负载用点（.）连接起来。
 * 使用指定的算法和密钥对连接后的字符串进行签名。
 * 将签名进行 Base64 编码。
 * 1. 解析 JWT
 * 首先，将 JWT 分解为三部分：Header、Payload 和 Signature。JWT 的格式通常是 Header.Payload.Signature，每部分之间用点（.）分隔。
 * 2. 验证签名
 * 签名部分用于验证 JWT 的完整性和真实性。验证签名的步骤如下：
 * 提取 Header 和 Payload：
 * 将 JWT 分解为 Header 和 Payload 部分。
 * 对 Header 和 Payload 进行 Base64 解码，得到原始的 JSON 字符串。
 * 重新生成签名：
 * 使用相同的算法和密钥，对 Header 和 Payload 重新生成签名。
 * 将 Header 和 Payload 用点（.）连接起来，形成一个字符串。
 * 使用密钥对这个字符串进行签名，生成一个新的签名。
 * 比较签名：
 * 将重新生成的签名与 JWT 中的签名部分进行比较。
 * 如果两个签名相同，说明 JWT 是有效的；否则，JWT 是无效的。
 * 3. 验证 Payload 中的声明
 * Payload 中包含了一些声明（claims），这些声明可以用于进一步验证 JWT 的有效性和安全性。常见的验证步骤包括：
 * 验证过期时间（exp）：
 * 检查 exp 声明，确保 JWT 没有过期。
 * 如果当前时间大于 exp 声明的时间，JWT 是无效的。
 * 验证签发时间（iat）：
 * 检查 iat 声明，确保 JWT 的签发时间是合理的。
 * 如果 iat 声明的时间在未来，JWT 是无效的。
 * 验证主题（sub）：
 * 检查 sub 声明，确保 JWT 的主题（通常是用户 ID）是有效的。
 * 如果 sub 声明的值不符合预期，JWT 是无效的。
 * 验证其他自定义声明：
 * 检查其他自定义声明，确保它们符合应用的要求。
 * 例如，验证用户角色、权限等。
 * 4. 验证算法
 * 确保 JWT 使用的签名算法与预期的算法一致。如果算法不匹配，JWT 是无效的。
 */

/**
 * Map<?, ?>：是一个通用的键值对集合，键和值的类型可以是任意的（取决于泛型参数），
 * 但在使用时需要手动进行类型转换，否则可能引发 ClassCastException。
 * Claims：通常是 JWT（JSON Web Token）库中专门设计的类型安全接口 / 类，
 * 对 JWT 中的标准声明（如 iss、sub、aud、exp 等）提供了类型安全的访问方法，无需手动类型转换。
 * 2. 结构约束
 * Map<?, ?>：仅提供基本的集合操作（如 get、put、containsKey 等）。
 * Claims：除了基本的键值对操作外，还可能提供与 JWT 相关的特定功能，例如：
 * 获取标准化的声明字段（如 getIssuer()、getExpiration()）。
 * 验证声明的有效性（如检查过期时间、签名验证）。
 * 转换为 JSON 格式。
 */
@Slf4j
@Component
public class JWTUtil {
    // 使用 @Slf4j 注解自动注入 Logger，无需手动声明
    private static final Logger logger = LoggerFactory.getLogger(
            JWTUtil.class);

    /**
     * 密钥, Base64编码的密钥
     * 这是一个静态常量，存储了用于生成和验证 JWT 的密钥。
     * 密钥应该是 Base64 编码的字符串，用于 HMAC SHA 签名算法。
     */
    private static final String SECRET = "VGe4g4BUYfTSfhM4os925dyEYLVEOoK9nohg37AoHFM=";

    /**
     * 生成安全密钥：将一个 Base64 编码的密钥解码并创建一个 HMAC SHA 密钥。
     * 这是一个静态常量，存储了解码后的密钥对象，这个才是真正用于 JWT 的签名和验证。
     */
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));

    /**
     * 过期时间 (毫秒)
     * 这是一个静态常量，定义了 JWT 的过期时间，单位是毫秒。
     * 在这个例子中，过期时间设置为 1 小时。
     */
    private static final long EXPIRATION = 60 * 60 * 1000;

    /**
     * 生成 jwt 令牌
     *
     * @param claim 自定义内容，通常包含用户信息或其他需要存储在 JWT 中的数据
     * @return 生成的 JWT 令牌字符串
     */
    public static String genJWT(Map<String, Object> claim) {
        //签名算法
        String jwt = Jwts.builder()
                .setClaims(claim) //自定义内容
                .setIssuedAt(new Date()) //设置签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))//设置过期时间
                .signWith(SECRET_KEY) //根据密钥进行签名算法计算
                .compact();// 将 JWT 转换为紧凑的字符串形式
        return jwt;
    }

    /**
     * 验证jwt令牌, 解析jwt令牌
     *
     * @param jwt
     * @return
     */
    public static Claims parseJWT(String jwt) {
        if (!StringUtils.hasLength(jwt)) {
            return null;
        }
        //创建解析器, 设置签名密钥
        //数字签名是不可以解密的, 只能进行比对
        //创建解析器并设置签名密钥
        //使用 Jwts.parserBuilder() 创建一个 JWT 解析器，并设置签名密钥。
        // 签名密钥用于验证 JWT 的签名部分，确保 JWT 没有被篡改。
        JwtParserBuilder jwtParserBuilder = Jwts.parserBuilder().setSigningKey(SECRET_KEY);
        Claims claims = null;

        try {
            //解析token
            //使用解析器解析 JWT，并获取其中的负载（Claims）。
            // 负载部分包含了 JWT 的具体信息，如用户信息、过期时间等
//            claims = jwtParserBuilder.build().parseClaimsJwt(jwt).getBody();  这是无签名的jwt解析
            claims = jwtParserBuilder.build().parseClaimsJws(jwt).getBody(); //这是有签名的jwt解析
        } catch (Exception e) {
            //签名验证失败
            log.error("解析令牌错误, jwt : {}", jwt, e);
        }
        //如果解析成功，返回解析后的 Claims 对象；如果解析失败，记录错误日志并返回 null。
        return claims;
    }

    public static Integer getUserIdFromToken(String jwtToken) {
        Claims claims = JWTUtil.parseJWT(jwtToken);
        if (claims != null) {
            Map<String, Object> userInfo = new HashMap<>(claims);
            return (Integer) userInfo.get("userId");
        }
        return null;
    }
}

