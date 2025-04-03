package com.project.cicd_auth.jwt;

import com.project.cicd_auth.service.ReissueService;
import com.project.cicd_auth.utils.CookieUtils;
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

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, ReissueService reissueService, CookieUtils cookieUtils) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.reissueService = reissueService;
        this.cookieUtils = cookieUtils;
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

        String access = jwtUtil.createJwt("access", username, role, jwtUtil.getAccessExpiry());

        String refresh = null;
        refresh = cookieUtils.findCookie("refresh", request);
        if (refresh != null && reissueService.existsByRefresh(refresh)) {
            reissueService.deleteByRefresh(refresh);
        }

        refresh = jwtUtil.createJwt("refresh", username, role, jwtUtil.getRefreshExpiry());

        //Refresh 토큰 저장
        reissueService.addRefreshEntity(username, refresh, jwtUtil.getRefreshExpiry());

        response.setHeader("authorization", "Bearer " + access);
        response.addCookie(cookieUtils.createCookie("refresh", refresh, 24 * 60 * 60));
        response.setStatus(HttpStatus.OK.value());
    }

    //로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}