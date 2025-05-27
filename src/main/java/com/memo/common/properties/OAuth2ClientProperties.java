package com.memo.common.properties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "spring.security.oauth2.client")
@Getter
@Setter
public class OAuth2ClientProperties { //화장성을 고려하여 kakao로 특정하지 않음
	private Map<String, Registration> registration = new HashMap<>();
	private Map<String, Provider> provider = new HashMap<>();

	@Getter
	@Setter
	public static class Registration {
		private String clientId;
		private String clientSecret;
		private String authorizationGrantType;
		private String redirectUri;
		private List<String> scope;
	}

	@Getter
	@Setter
	public static class Provider {
		private String authorizationUri;
		private String tokenUri;
		private String userInfoUri;
		private String userNameAttribute;
	}

}

