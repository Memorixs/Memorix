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

	public void save(Long userId, String kindOfToken, String value) {
		redisTemplate.opsForValue().set(UtilString.TOKEN_IDENTIFIER.value() + userId + kindOfToken, value);
	}
	public String find(Long userId, String kindOfToken) {
		return redisTemplate.opsForValue().get(UtilString.TOKEN_IDENTIFIER.value() + userId + kindOfToken);
	}
	public void delete(Long userId, String kindOfToken) {
		redisTemplate.opsForValue().getAndDelete(UtilString.TOKEN_IDENTIFIER.value() + userId + kindOfToken);
	}

}
