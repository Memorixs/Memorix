package com.memo.user.oauth;

import com.memo.user.entity.LoginType;

public interface OAuthUser {
	String getEmail();
	String getUsername();
	String getId();
	String getProfileImg();
	LoginType getLoginType();
}
