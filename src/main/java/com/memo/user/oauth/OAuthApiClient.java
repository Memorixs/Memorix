package com.memo.user.oauth;

public interface OAuthApiClient {
	TokenResponse requestAccessToken(String code);
	OAuthUserResponse requestOAuthUser(String token);
}
