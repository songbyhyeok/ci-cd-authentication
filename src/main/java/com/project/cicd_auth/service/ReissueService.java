package com.project.cicd_auth.service;

import com.project.cicd_auth.entity.RefreshEntity;
import com.project.cicd_auth.repository.RefreshRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class ReissueService {
    private final RefreshRepository refreshRepository;

//    public validateAndReissueToken() {
//
//    }

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
