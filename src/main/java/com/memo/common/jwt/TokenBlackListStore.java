package com.memo.common.jwt;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

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
