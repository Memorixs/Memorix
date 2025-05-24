package com.memo.login.oauth.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class KakaoTokenInfo {
	private Long id;
	@JsonProperty("expires_in")
	private Integer expiresIn;
	@JsonProperty("app_id")
	private Integer appId;
}
