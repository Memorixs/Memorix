package com.memo.login.oauth.kakao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class KakaoInfo {
	@JsonProperty("kakao_account")
	private KakaoAccount kakaoAccount;
	private Long id;

	@Getter
	static class KakaoAccount {
		private KakaoProfile profile;
		private String email;
	}

	@Getter
	static class KakaoProfile {
		private String nickname;
	}


	public String getEmail() {
		return kakaoAccount.getEmail();
	}


	public String getNickname() {
		return kakaoAccount.getProfile().getNickname();
	}


	public String getId() {
		return String.valueOf(id);
	}
}
