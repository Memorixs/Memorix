package com.memo.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class RefreshTokenStore {
	//톰캣의 세션 기능을 껏으니 애플리케이션에 임시 저장소를 만들어서 사용
	private final Map<String, String> store = new ConcurrentHashMap<>();

	public void save(String userId, String refreshToken) {
		store.put(refreshToken, userId);
	}

	public String get(String refreshToken) {
		return store.get(refreshToken);
	}

	public void remove(String refreshToken) {
		store.remove(refreshToken);
	}

	public boolean containsKey(String refreshToken) {
		return store.containsKey(refreshToken);
	}
}

