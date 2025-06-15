package com.memo.user.oauth.google;

import java.util.Map;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.memo.user.oauth.OAuthService;
import com.memo.user.oauth.OAuthUserResponse;

@Service
public class GoogleOAuthService implements OAuthService {
	@Override
	public OAuthUserResponse service(OAuth2User oAuth2User) {
		Map<String, Object> attributes = oAuth2User.getAttributes();
		String picture = (String)attributes.get("picture");
		String id = (String)attributes.get("sub");
		String email = (String)attributes.get("email");

		return GoogleUserResponse.of(email, id, picture);
	}
}
