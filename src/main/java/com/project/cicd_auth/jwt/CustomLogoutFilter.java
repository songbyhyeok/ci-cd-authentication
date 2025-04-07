package com.project.cicd_auth.jwt;

import com.project.cicd_auth.repository.RefreshRepository;
import com.project.cicd_auth.service.ReissueService;
import com.project.cicd_auth.utils.CookieUtils;
import com.project.cicd_auth.utils.RedisUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.io.PrintWriter;

public class CustomLogoutFilter extends GenericFilterBean {

    private final JWTUtil jwtUtil;
    private final CookieUtils cookieUtils;
    private final RedisUtil redisUtil;
    private final RefreshRepository refreshRepository;
    private final ReissueService reissueService;

    public CustomLogoutFilter(JWTUtil jwtUtil, CookieUtils cookieUtils, RedisUtil redisUtil, RefreshRepository refreshRepository, ReissueService reissueService) {
        this.jwtUtil = jwtUtil;
        this.cookieUtils = cookieUtils;
        this.redisUtil = redisUtil;
        this.refreshRepository = refreshRepository;
        this.reissueService = reissueService;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        doFilter((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse, filterChain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        //path and method verify
        String requestUri = request.getRequestURI();
        // ^: 문자열의 시작을 의미
        // \\/는 자바에서 슬래시(/) 문자를 문자 그대로 사용하기 위한 이스케이프 시퀀스
        // 슬래시는 특수 문자로 취급되기 때문에 백슬래시(\)를 두 번 사용하여 \\/로 표기
        // $: 문자열의 끝을 의미
        // requestUri.matches("^\\/logout$")는 requestUri가 /logout 문자열과 정확히 일치하는지 확인
        if (!requestUri.matches("^\\/logout$")) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestMethod = request.getMethod();
        if (!requestMethod.equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String refreshToken = reissueService.findRefreshToken(request, response);
        if (refreshToken == null) {
            PrintWriter writer = response.getWriter();
            writer.print("refresh token is null");
        }

        // Refresh Token을 검증하고 Redis에서 제거, Blacklist에 추가한다.
        if (reissueService.validateRefreshToken(refreshToken, response)) {
            final String username = jwtUtil.getUsername(refreshToken);
            redisUtil.delete(username);
            redisUtil.setBlacklistEntries(refreshToken, username, 300000L);
        } else {
            PrintWriter writer = response.getWriter();
            writer.print("refresh Token이 만료되었습니다.");
        }

        response.addCookie(cookieUtils.createCookie("Refresh", null, 600000));
        response.setStatus(HttpServletResponse.SC_OK);
        filterChain.doFilter(request, response);
    }
}
