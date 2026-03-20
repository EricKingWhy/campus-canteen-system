package fun.cyhgraph.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Slf4j
public class JwtUtil {
    /**
     * 生成jwt
     * 使用Hs256算法, 私匙使用固定秘钥
     *
     * @param secretKey jwt秘钥
     * @param ttlMillis jwt过期时间(毫秒)
     * @param claims    设置的信息
     * @return JWT Token 字符串
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        log.info("开始生成JWT Token...");

        // 1. 指定签名算法
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        // 2. 计算过期时间
        long expMillis = System.currentTimeMillis() + ttlMillis;
        Date exp = new Date(expMillis);

        // 3. 【核心修复】使用 SecretKeySpec 创建密钥，避免 JAXB Base64Codec 问题
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        Key signingKey = new SecretKeySpec(keyBytes, signatureAlgorithm.getJcaName());

        // 4. 构建 JWT 【注意：jjwt 0.9.1 参数顺序是 (SignatureAlgorithm, Key)】
        JwtBuilder builder = Jwts.builder()
                .setClaims(claims)
                .signWith(signatureAlgorithm, signingKey) // 正确的参数顺序！
                .setExpiration(exp);

        String token = builder.compact();
        log.info("JWT Token 生成成功！");
        return token;
    }

    /**
     * Token解密
     *
     * @param secretKey jwt秘钥
     * @param token     加密后的token
     * @return Claims 对象
     */
    public static Claims parseJWT(String secretKey, String token) {
        log.info("开始解析JWT Token...");

        // 使用 SecretKeySpec 创建密钥
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        Key signingKey = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());

        Claims claims = Jwts.parser()
                .setSigningKey(signingKey)
                .parseClaimsJws(token)
                .getBody();

        log.info("JWT Token 解析成功，claims: {}", claims);
        return claims;
    }
}
