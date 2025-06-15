package com.memo.common.util;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum UtilString {
	BEARER("Bearer "),
	AUTHORIZATION("Authorization"),
	COOKIE_NAME("refresh-token"),
	EMAIL_AUTH_URL("http://localhost:8080/api/auth/confirm?email="),
	KAKAO_ACCESS_TOKEN(":kakaoAccessToken"),
	KAKAO_REFRESH_TOKEN(":kakaoRefreshToken"),
	SERVICE_REFRESH_TOKEN(":serviceRefreshToken"),
	TOKEN_IDENTIFIER("user:"),
	BLACKLIST_TOKEN(":blackListToken");
	;
	private final String value;

	public String value() {
		return value;
	}
}
