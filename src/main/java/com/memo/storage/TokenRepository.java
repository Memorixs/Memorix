package com.memo.storage;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TokenRepository {
	private final RedisTemplate<String, String> redisTemplate;
	public void save(String key, String value, long timeout, TimeUnit unit) {
		redisTemplate.opsForValue().set(key, value, timeout, unit);
	}

	public void save(String key, String value) {
		redisTemplate.opsForValue().set(key, value);
	}
	public String findByKey(String key) {
		return redisTemplate.opsForValue().get(key);
	}
	public void deleteByKey(String key) {
		redisTemplate.opsForValue().getAndDelete(key);
	}

}
