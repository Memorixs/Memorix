package com.memo.common.util;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum UtilString {
	BEARER("Bearer "),
	AUTHORIZATION("Authorization"),
	EXCEPTION("EXCEPTION"),
	EXPIRED("EXPIRED"),
	COOKIE_NAME("refresh-token"),
	EMAIL_AUTH_URL("http://localhost:8080/api/auth/confirm?email="),
	;
	private final String value;

	public String value() {
		return value;
	}
}
