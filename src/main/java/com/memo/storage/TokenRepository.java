package com.memo.storage;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.memo.common.util.UtilString;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TokenRepository {
	private final RedisTemplate<String, String> redisTemplate;
	public void save(String key, String value, long timeout, TimeUnit unit) {
		redisTemplate.opsForValue().set(key, value, timeout, unit);
	}

	public void save(String key, String value) {
		redisTemplate.opsForValue().set(UtilString.TOKEN_IDENTIFIER.value()+key, value);
	}
	public String findByKey(String key) {
		return redisTemplate.opsForValue().get(UtilString.TOKEN_IDENTIFIER.value()+key);
	}
	public void deleteByKey(String key) {
		redisTemplate.opsForValue().getAndDelete(UtilString.TOKEN_IDENTIFIER.value()+key);
	}

}
