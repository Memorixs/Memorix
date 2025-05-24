package com.memo.common;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum UtilString {
	BEARER("Bearer "),
	AUTHORIZATION("Authorization"),
	EXCEPTION("EXPIRED"),
	EXPIRED("EXPIRED"),
	COOKIE_NAME("refresh-token"),
	;
	private final String value;

	public String value() {
		return value;
	}
}
