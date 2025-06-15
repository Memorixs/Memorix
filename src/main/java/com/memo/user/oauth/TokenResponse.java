package com.memo.user.oauth;

public interface TokenResponse {
	String getAccessToken();
	String getRefreshToken();
	Integer getExpiresIn();
	Integer getRefreshTokenExpiresIn();
}
