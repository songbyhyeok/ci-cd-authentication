package com.project.cicd_auth.controller;

import com.project.cicd_auth.jwt.JWTUtil;
import com.project.cicd_auth.service.ReissueService;
import com.project.cicd_auth.utils.CookieUtils;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReissueController {
    private final JWTUtil jwtUtil;
    private final CookieUtils cookieUtils;
    private final ReissueService reissueService;

    public ReissueController(JWTUtil jwtUtil, CookieUtils cookieUtils, ReissueService reissueService) {
        this.jwtUtil = jwtUtil;
        this.cookieUtils = cookieUtils;
        this.reissueService = reissueService;
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissueToken(HttpServletRequest request, HttpServletResponse response) {
        String refresh = null;
        refresh = cookieUtils.findCookie("refresh", request.getCookies());
        if (refresh == null) {
            return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
        }

        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            return new ResponseEntity<>("refresh token expired", HttpStatus.BAD_REQUEST);
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(refresh);
        if (!category.equals("Refresh")) {
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        //DB에 저장되어 있는지 확인
        boolean isExist = reissueService.existsByRefresh(refresh);
        if (!isExist) {
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        final String username = jwtUtil.getUsername(refresh);
        final String role = jwtUtil.getRole(refresh);

        //make new JWT
        String newAccess = jwtUtil.createJwt("access", username, role, jwtUtil.getAccessExpiry());
        String newRefresh = jwtUtil.createJwt("refresh", username, role, jwtUtil.getRefreshExpiry());

        //Refresh 토큰 저장 DB에 기존의 Refresh 토큰 삭제 후 새 Refresh 토큰 저장
        reissueService.deleteByRefresh(refresh);
        reissueService.addRefreshEntity(username, newRefresh, jwtUtil.getRefreshExpiry());

        //response
        response.setHeader("authorization", "Bearer " + newAccess);
        response.addCookie(cookieUtils.createCookie("Refresh", newRefresh, 24 * 60 * 60));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
