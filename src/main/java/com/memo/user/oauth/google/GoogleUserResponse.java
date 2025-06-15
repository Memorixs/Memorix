package com.memo.user.oauth.google;

import com.memo.user.entity.LoginType;
import com.memo.user.oauth.OAuthUserResponse;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access= AccessLevel.PRIVATE)
public class GoogleUserResponse implements OAuthUserResponse {
	private String email;
	private String id;
	private String picture;
	private LoginType loginType;

	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public String getUsername() {
		return id;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getProfileImg() {
		return picture;
	}

	@Override
	public LoginType getLoginType() {
		return loginType;
	}

	public static GoogleUserResponse of(String email, String id, String picture) {
		return new GoogleUserResponse(email, id, picture, LoginType.GOOGLE);
	}

}
