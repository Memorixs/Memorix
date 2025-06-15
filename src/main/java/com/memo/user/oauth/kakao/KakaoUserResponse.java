package com.memo.user.oauth.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.memo.user.entity.LoginType;
import com.memo.user.oauth.OAuthUserResponse;

import lombok.Getter;

@Getter
public class KakaoUserResponse implements OAuthUserResponse {
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


	public String getUsername() {
		return kakaoAccount.getProfile().getNickname();
	}


	public String getId() {
		return String.valueOf(id);
	}

	@Override
	public String getProfileImg() {
		return "";
	}

	@Override
	public LoginType getLoginType() {
		return LoginType.KAKAO;
	}
}
