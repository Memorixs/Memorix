package com.memo.user.oauth.google;

import java.util.Map;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.memo.user.oauth.OAuthService;
import com.memo.user.oauth.OAuthUser;

@Service
public class GoogleOAuthService implements OAuthService {
	@Override
	public OAuthUser service(OAuth2User oAuth2User) {
		Map<String, Object> attributes = oAuth2User.getAttributes();
		String picture = (String)attributes.get("picture");
		String id = (String)attributes.get("sub");
		String email = (String)attributes.get("email");

		return GoogleUser.of(email, id, picture);
	}
}
