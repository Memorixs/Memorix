package com.memo.user.oauth;

import com.memo.user.entity.LoginType;

public interface OAuthUserResponse {
	String getEmail();
	String getUsername();
	String getId();
	String getProfileImg();
	LoginType getLoginType();
}
