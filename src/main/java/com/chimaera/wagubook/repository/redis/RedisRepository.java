package com.chimaera.wagubook.repository.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisRepository {
    private final RedisTemplate<String, Object> redisTemplate;

    /* 등록, 수정 */
    // 새로운 Key-Value 쌍을 추가하거나, 해당 Key가 존재하는 경우 기존 값을 덮어쓴다.
    public void setValues(String key, String value) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(key, value);
    }

    // 유효 시간까지 고려할 경우 (Duration 객체 사용)
    // 사용 예시) redisService.setValues("key", "value", Duration.ofMinutes(5));
    public void setValues(String key, String value, Duration duration) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(key, value);
        redisTemplate.expire(key, duration);
    }

    // 유효 시간까지 고려할 경우 (Duration 객체 사용)
    // 사용 예시) redisService.setValues("key", "value", Duration.ofMinutes(5));
    public void setValuesObject(String key, Object value, Duration duration) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(key, value);
        redisTemplate.expire(key, duration);
    }

    /* 조회 */
    // Key를 기반으로 Value 조회
    public String getValue(String key) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        if (values.get(key) == null) return null;
        return String.valueOf(values.get(key));
    }

    /* 조회 */
    // Key를 기반으로 Value 조회
    public Object getObject(String key) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        if (values.get(key) == null) return null;
        return values.get(key);
    }

    /* 삭제 */
    public void deleteValue(String key) {
        redisTemplate.delete(key);
    }
}
