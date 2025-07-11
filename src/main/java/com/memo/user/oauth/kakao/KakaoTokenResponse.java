package com.memo.user.oauth.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.memo.user.oauth.TokenResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class KakaoTokenResponse implements TokenResponse {
	@JsonProperty("token_type")
	private String tokenType;
	@JsonProperty("access_token")
	private String accessToken;
	@JsonProperty("expires_in")
	private Integer expiresIn;
	@JsonProperty("refresh_token")
	private String refreshToken;
	@JsonProperty("refresh_token_expires_in")
	private Integer refreshTokenExpiresIn;
}
