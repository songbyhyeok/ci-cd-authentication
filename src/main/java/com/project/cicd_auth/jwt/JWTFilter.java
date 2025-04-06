package com.project.cicd_auth.jwt;

import com.project.cicd_auth.dto.CustomUserDetails;
import com.project.cicd_auth.entity.Member;
import com.project.cicd_auth.service.ReissueService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;
    private final ReissueService reissueService;

    public JWTFilter(JWTUtil jwtUtil, ReissueService reissueService) {
        this.jwtUtil = jwtUtil;
        this.reissueService = reissueService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        StringBuilder accessToken = new StringBuilder();
        if (validateAccessToken(accessToken, request, response, filterChain)) {
            setSecurityContextWithUserDetails(accessToken.toString());
            filterChain.doFilter(request, response);
        }
    }

    private boolean validateAccessToken(StringBuilder accessToken, HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 헤더에서 access 토큰을 꺼냄
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            System.out.println("token null");
            filterChain.doFilter(request, response);
            return false;
        }

        accessToken.append(authorization.split(" ")[1]);
        if (accessToken.isEmpty()) {
            System.out.println("accessToken null");
            filterChain.doFilter(request, response);
            return false;
        }

        // Access Token이 만료됐다면 서버는 Refresh Token을 Client에게 요청한다.
        boolean isAccessTokenExpired = false;
        try {
            jwtUtil.isExpired(accessToken.toString());
        } catch (ExpiredJwtException e) {
            isAccessTokenExpired = true;
            boolean isRefreshTokenValid = reissueService.processRefreshToken(request, response);
            // Refresh Token도 만료됐다면 재로그인을 요청한다.
            if (!isRefreshTokenValid) {
                System.out.println("다시 로그인해야 합니다.");
            }

            return false;
        }

        // 토큰이 access인지 확인 (발급시 페이로드에 명시)
        if (!jwtUtil.getCategory(accessToken.toString()).equals("access")) {
            PrintWriter writer = response.getWriter();
            writer.print("invalid access token");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        return true;
    }

    private void setSecurityContextWithUserDetails(final String accessToken) {
        String username = jwtUtil.getUsername(accessToken);
        String role = jwtUtil.getRole(accessToken);

        Member userEntity = new Member();
        userEntity.setUsername(username);
        userEntity.setRole(role);
        CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);

        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
