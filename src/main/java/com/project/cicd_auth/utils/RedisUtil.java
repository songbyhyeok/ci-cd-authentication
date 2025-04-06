package com.project.cicd_auth.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {
    private final RedisTemplate<String, String> refreshTokenRedisTemplate;
    private final RedisTemplate<String, String> blacklistRedisTemplate;

    public RedisUtil(@Qualifier("redisStringTemplate") RedisTemplate<String, String> refreshTokenRedisTemplate,
                     @Qualifier("redisStringTemplate2") RedisTemplate<String, String> blackListRedisTemplate) {
        this.refreshTokenRedisTemplate = refreshTokenRedisTemplate;
        this.blacklistRedisTemplate = blackListRedisTemplate;
    }

    public void set(String key, String value, Long milliSeconds) {
        refreshTokenRedisTemplate.setValueSerializer(new StringRedisSerializer());
        refreshTokenRedisTemplate.opsForValue().set(key, value, milliSeconds, TimeUnit.MILLISECONDS);
    }

    public String get(String key) {
        return refreshTokenRedisTemplate.opsForValue().get(key);
    }

    public boolean hasKey(String key) {
        return refreshTokenRedisTemplate.hasKey(key);
    }

    public boolean delete(String key) {
        return refreshTokenRedisTemplate.delete(key);
    }

    public void setBlacklistEntries(String key, String value, Long milliSeconds) {
        blacklistRedisTemplate.setValueSerializer(new StringRedisSerializer());
        blacklistRedisTemplate.opsForValue().set(key, value, milliSeconds, TimeUnit.MILLISECONDS);
    }

    public String getBlacklistEntries(String key) {
        return blacklistRedisTemplate.opsForValue().get(key);
    }

    public boolean hasKeyBlacklistEntries(String key) {
        return blacklistRedisTemplate.hasKey(key);
    }

    public boolean deleteBlacklistEntries(String key) {
        return blacklistRedisTemplate.delete(key);
    }
}
