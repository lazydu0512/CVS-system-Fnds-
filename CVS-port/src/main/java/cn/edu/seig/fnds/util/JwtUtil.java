package cn.edu.seig.fnds.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 
 * @author lazzydu
 */
@Component
public class JwtUtil {

    // JWT密钥（生产环境应该从配置文件读取）
    private static final String SECRET_KEY = "CVS-System-Secret-Key-2024-Very-Long-Key-For-Security";

    // Token有效期：7天
    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000L;

    private final SecretKey key;

    public JwtUtil() {
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT Token
     * 
     * @param userId   用户ID
     * @param username 用户名
     * @param role     用户角色
     * @return JWT Token
     */
    public String generateToken(Long userId, String username, Integer role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析Token获取Claims
     * 
     * @param token JWT Token
     * @return Claims
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            System.out.println("Token已过期: " + e.getMessage());
            return null;
        } catch (JwtException e) {
            System.out.println("Token解析失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中获取用户ID
     * 
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            Object userId = claims.get("userId");
            if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            } else if (userId instanceof Long) {
                return (Long) userId;
            }
        }
        return null;
    }

    /**
     * 验证Token是否有效
     * 
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        return parseToken(token) != null;
    }
}
