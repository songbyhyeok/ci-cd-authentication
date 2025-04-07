package com.project.cicd_auth.jwt;

import com.project.cicd_auth.service.ReissueService;
import com.project.cicd_auth.utils.CookieUtils;
import com.project.cicd_auth.utils.RedisUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final ReissueService reissueService;
    private final CookieUtils cookieUtils;
    private final RedisUtil redisUtil;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, ReissueService reissueService, CookieUtils cookieUtils, RedisUtil redisUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.reissueService = reissueService;
        this.cookieUtils = cookieUtils;
        this.redisUtil = redisUtil;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        //클라이언트 요청에서 username, password 추출
        String username = obtainUsername(request);
        String password = obtainPassword(request);

        //스프링 시큐리티에서 username과 password를 검증하기 위해서는 token에 담아야 함
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);

        //token에 담은 검증을 위한 AuthenticationManager로 전달
        return authenticationManager.authenticate(authToken);
    }

    //로그인 성공시 실행하는 메소드 (여기서 JWT를 발급하면 됨)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        String username = authResult.getName();

        Collection<? extends GrantedAuthority> authorities = authResult.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        // Redis에 Refresh Token이 이미 있다면 Redis에서 삭제 후에 아직 만료 기간이 남아있다면 Blacklist에 추가한다.
        if (redisUtil.hasKey(username)) {
            final String refreshToken = redisUtil.get(username);
            redisUtil.delete(username);

            if (reissueService.validateRefreshToken(refreshToken, response)) {
                redisUtil.setBlacklistEntries(refreshToken, username, 300000L);
            } else {
                System.out.println("Refresh Token이 만료되었습니다.");
            }
        }

        // 새로운 Access와 Refresh Token을 발급한다.
        final String newAccess = jwtUtil.createJwt("access", username, role, jwtUtil.getAccessExpiry());
        final String newRefresh = jwtUtil.createJwt("refresh", username, role, jwtUtil.getRefreshExpiry());

        redisUtil.set(username, newRefresh, 300000L);

        response.setHeader("Authorization", "Bearer " + newAccess);
        response.addCookie(cookieUtils.createCookie("Refresh", newRefresh, 24 * 60 * 60));
        response.setStatus(HttpStatus.OK.value());
    }

    //로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}