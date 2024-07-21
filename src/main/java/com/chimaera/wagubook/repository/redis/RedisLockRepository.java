package com.chimaera.wagubook.repository.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisLockRepository {
    private final RedisTemplate<String, Object> redisTemplate;

    public Boolean lock(String key){
        return redisTemplate
                .opsForValue()
                .setIfAbsent(key, "lock", Duration.ofMillis(3_000));
    }

    public Boolean unlock(String key){
        return redisTemplate.delete(key);
    }

}
