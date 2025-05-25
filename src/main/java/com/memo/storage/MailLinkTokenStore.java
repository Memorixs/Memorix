package com.memo.storage;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class MailLinkTokenStore {
	private final Map<String, Date> store = new ConcurrentHashMap<>();

	public void save(String email, Date expired) {
		store.put(email, expired);
	}

	public Date get(String email) {
		return store.get(email);
	}

	public void remove(String email) {
		store.remove(email);
	}
}
