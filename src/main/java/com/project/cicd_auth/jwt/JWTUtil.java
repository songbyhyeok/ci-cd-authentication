package com.project.cicd_auth.jwt;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTUtil {
    private SecretKey secretKey;

    @Value("${spring.jwt.access_expiry}")
    private long accessExpiry;

    @Value("${spring.jwt.refresh_expiry}")
    private long refreshExpiry;

    public JWTUtil(@Value("${spring.jwt.secret}")String secret) {
        // secret 값을 바이트 배열로 변환한 후, HS256 알고리즘으로 서명할 SecretKey 객체를 생성
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public String getCategory(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("category", String.class);
    }

    public String getUsername(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("username", String.class);
    }

    public String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    public Boolean isExpired(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token). // JWT에서 서명된 클레임을 파싱
                getPayload().getExpiration().
                before(new Date()); // 현재 시간이 만료일보다 이후인지 확인
    }

    public long getAccessExpiry() {
        return accessExpiry;
    }

    public long getRefreshExpiry() {
        return refreshExpiry;
    }

    public String createJwt(String category, String username, String role, Long expiredMs) {
        return Jwts.builder()
                .claim("category", category)
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis())) // 발행일을 현재 시간으로 설정
                .expiration(new Date(System.currentTimeMillis() + expiredMs)) // 만료일을 현재 시간 + expiredMs로 설정
                .signWith(secretKey)
                .compact(); // JWT 문자열로 변환하여 반환
    }
}
