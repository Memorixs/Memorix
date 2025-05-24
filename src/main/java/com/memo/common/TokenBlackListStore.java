package com.memo.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import lombok.ToString;

@Component
public class TokenBlackListStore {
	private final List<String> store = new ArrayList<>();

	public void save(String token) {
		store.add(token);
	}

	public boolean contains(String token) {
		return store.contains(token);
	}

}
