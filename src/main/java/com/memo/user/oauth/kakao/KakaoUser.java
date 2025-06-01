package com.memo.user.oauth.kakao;

import com.memo.user.entity.LoginType;
import com.memo.user.oauth.OAuthUser;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access= AccessLevel.PRIVATE)
public class KakaoUser implements OAuthUser {
	private String email; //카카오의 경우 사용자 정보 조회에서 이메일 조회는 권한이 없으므로 이메일을 따로 가입하도록 해야함
	private String nickname;
	private String id;
	private String profileImage;
	private LoginType loginType;
	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public String getUsername() {
		return nickname;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getProfileImg() {
		return profileImage;
	}

	@Override
	public LoginType getLoginType() {
		return loginType;
	}

	public static KakaoUser of(String email, String nickname, String id, String profileImage) {
		return new KakaoUser(email, nickname, id, profileImage, LoginType.KAKAO);
	}
}
