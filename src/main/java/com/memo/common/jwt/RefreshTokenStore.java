package com.memo.common.jwt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import lombok.ToString;

@Component
@ToString
public class RefreshTokenStore {
	//톰캣의 세션 기능을 껏으니 애플리케이션에 임시 저장소를 만들어서 사용
	private final Map<Long, String> store = new ConcurrentHashMap<>();

	public void save(Long userId, String refreshToken) {
		store.put(userId, refreshToken);
	}

	public String get(Long userId) {
		return store.get(userId);
	}

	public void remove(Long userId) {
		store.remove(userId);
	}

	public boolean containsKey(Long userId) {
		return store.containsKey(userId);
	}
}

