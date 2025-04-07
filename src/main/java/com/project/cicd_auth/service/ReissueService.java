package com.project.cicd_auth.service;

import com.project.cicd_auth.entity.RefreshEntity;
import com.project.cicd_auth.jwt.JWTUtil;
import com.project.cicd_auth.repository.RefreshRepository;
import com.project.cicd_auth.utils.CookieUtils;
import com.project.cicd_auth.utils.RedisUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReissueService {
    private final RefreshRepository refreshRepository;
    private final CookieUtils cookieUtils;
    private final JWTUtil jwtUtil;
    private final RedisUtil redisUtil;

    // RefreshToken을 재발급하거나, 블랙리스트에 추가한다.
    public boolean processRefreshToken(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String refreshToken = findRefreshToken(request, response);
        if (!validateRefreshToken(refreshToken, response)) {
            return false;
        }

        if (rejectReplayAttack(refreshToken, response)) {
            return false;
        }

        // Access Token이 만료됐고, 그리고 Refresh Token이 유효하므로 Refresh Token Rotation을 호출한다.
        generateAccessAndRefreshTokens(refreshToken, response);
        return true;
    }

    public String findRefreshToken(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String refreshToken = null;
        refreshToken = cookieUtils.findCookie("Refresh", request.getCookies());
        if (refreshToken == null) {
            PrintWriter writer = response.getWriter();
            writer.print("refresh token null");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        return refreshToken;
    }

    public boolean validateRefreshToken(String refreshToken, HttpServletResponse response) throws ServletException, IOException {
        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            PrintWriter writer = response.getWriter();
            writer.print("refresh token expired");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        if (!jwtUtil.getCategory(refreshToken).equals("refresh")) {
            PrintWriter writer = response.getWriter();
            writer.print("invalid refresh token");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        }

        return true;
    }

    private boolean rejectReplayAttack(final String refreshToken, HttpServletResponse response) {
        // 블랙리스트에 추가된 Refresh Token인지 확인한다.
        String refreshTokenTest = "eyJhbGciOiJIUzI1NiJ9.eyJjYXRlZ29yeSI6InJlZnJlc2giLCJ1c2VybmFtZSI6ImFkbWluIiwicm9sZSI6IlJPTEVfQURNSU4iLCJpYXQiOjE3NDM5MTQ3MTEsImV4cCI6MTc0MzkxNTMxMX0.lcEdN0UfrzUdE5KkvKY3bGM0N1Wdcx8Hbo1W_d1nLtA";
        if (redisUtil.hasKeyBlacklistEntries(refreshTokenTest)) {
            // Replay Attack 감지
            // 두 가지 경우로 나뉜다.
            // 1. 클라이언트가 재발급을 마친 상태에서, 이미 이전에 해커가 클라이언트의 토큰을 탈취한 다음 재발급을 신청한 경우
            // 2. 해커가 클라이언트의 토큰을 탈취 후에 재발급을 마친 상태에서 클라이언트가 재발급을 신청한 경우
            // 서버는 재발급 대상자를 식별할 수 없으므로, 이미 재발급된 토큰은 블랙리스트에 추가하고, 현재 재발급은 무효 처리한다.
            final String userInfo = redisUtil.getBlacklistEntries(refreshTokenTest);
            if (redisUtil.hasKey(userInfo)) {
                final String issuedRefreshToken = redisUtil.get(userInfo);
                if (issuedRefreshToken != null) {
                    redisUtil.delete(userInfo);
                    redisUtil.setBlacklistEntries(issuedRefreshToken, userInfo, 300000L);
                }
            }

            System.out.println("Replay Attack 감지");
            response.addCookie(cookieUtils.createCookie("Refresh", null, 600000));

            return true;
        }

        return false;
    }

    private void generateAccessAndRefreshTokens(String refreshToken, HttpServletResponse response) {
        final String username = jwtUtil.getUsername(refreshToken);
        final String role = jwtUtil.getRole(refreshToken);

        // 이전 Refresh Token을 Redis에서 제거, Blacklist에 추가한다.
        redisUtil.delete(username);
        redisUtil.setBlacklistEntries(refreshToken, username, 300000L);

        String newAccess = jwtUtil.createJwt("access", username, role, jwtUtil.getAccessExpiry());
        String newRefresh = jwtUtil.createJwt("refresh", username, role, jwtUtil.getRefreshExpiry());

        // 새로 생성된 Refresh Token은 Redis에서 관리한다.
        redisUtil.set(username, newRefresh, 300000L);

        response.setHeader("Authorization", "Bearer " + newAccess);
        response.addCookie(cookieUtils.createCookie("Refresh", newRefresh, 24 * 60 * 60));
    }

    public boolean existsByRefresh(String refresh) {
        return refreshRepository.existsByRefresh(refresh);
    }

    public void deleteByRefresh(String refresh) {
        refreshRepository.deleteByRefresh(refresh);
    }

    public void addRefreshEntity(String username, String refresh, Long expiredMs) {
        Date date = new Date(System.currentTimeMillis() + expiredMs);

        RefreshEntity refreshEntity = new RefreshEntity();
        refreshEntity.setUsername(username);
        refreshEntity.setRefresh(refresh);
        refreshEntity.setExpiration(date.toString());

        refreshRepository.save(refreshEntity);
    }
}
