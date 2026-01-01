package com.zyx.image.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration:604800000}")
    private Long expiration;
    
    private SecretKey getSigningKey() {
        byte[] keyBytes;
        try {
            // 尝试将 secret 作为 Base64 字符串解码
            // 如果配置的是 Base64 编码的密钥（推荐），则解码
            keyBytes = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException e) {
            // 如果不是 Base64 格式，则作为普通字符串使用
            // 但需要确保长度足够（HS512 需要至少 64 字节）
            byte[] utf8Bytes = secret.getBytes(StandardCharsets.UTF_8);
            if (utf8Bytes.length < 64) {
                throw new IllegalArgumentException(
                    "JWT secret must be at least 64 bytes (512 bits) for HS512 algorithm. " +
                    "Current length: " + utf8Bytes.length + " bytes. " +
                    "Consider using a Base64-encoded key with at least 88 characters.");
            }
            keyBytes = utf8Bytes;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * 生成Token
     */
    public String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        return createToken(claims);
    }
    
    /**
     * 创建Token
     */
    private String createToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * 从Token中获取Claims
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.valueOf(claims.get("userId").toString());
    }
    
    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("username").toString();
    }
    
    /**
     * 验证Token是否过期
     */
    public Boolean isTokenExpired(String token) {
        Claims claims = getClaimsFromToken(token);
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }
    
    /**
     * 验证Token（会抛出异常，如 ExpiredJwtException）
     */
    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }
}

