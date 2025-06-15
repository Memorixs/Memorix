package com.memo.user.oauth;

import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuthService {
	OAuthUserResponse service(OAuth2User oAuth2User);
}
