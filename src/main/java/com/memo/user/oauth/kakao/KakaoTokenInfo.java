package com.memo.user.oauth.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoTokenInfo {
	private Long id;
	@JsonProperty("expires_in")
	private Integer expiresIn;
	@JsonProperty("app_id")
	private Integer appId;
}
