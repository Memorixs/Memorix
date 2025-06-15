package com.memo.user.oauth.kakao;

import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.beans.factory.annotation.Value;

import com.memo.user.oauth.OAuthType;

import lombok.Getter;

@Getter
@Component
public class KakaoRequest {

	private final OAuthType provider = OAuthType.KAKAO;

	@Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
	private String tokenUrl;
	@Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
	private String userInfoUrl;
	@Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
	private String redirectUrl;
	@Value("${spring.security.oauth2.client.registration.kakao.client-id}")
	private String clientId;

	private final String GRANT_TYPE = "authorization_code";


	public MultiValueMap<String, String> makeBody(String code) {
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("code", code);
		body.add("grant_type", GRANT_TYPE);
		body.add("client_id", clientId);
		body.add("redirect_uri", redirectUrl);

		return body;
	}

	public MultiValueMap<String, String> makeBody(String grantType, String token) {
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("grant_type", grantType);
		body.add("client_id", clientId);
		body.add("refresh_token", token);
		return body;
	}

	public static HttpHeaders makeHeader(@Nullable MediaType mediaType, @Nullable Map<String, String> headers) {
		HttpHeaders httpHeaders = new HttpHeaders();
		if(mediaType != null){
			httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		}
		if (headers != null && !headers.isEmpty()) {
			for (String key : headers.keySet()) {
				httpHeaders.set(key, headers.get(key));
			}
		}
		return httpHeaders;
	}

	public static HttpEntity<MultiValueMap<String, String>> makeRequestEntity(@Nullable MediaType mediaType, @Nullable Map<String, String> headers, @Nullable MultiValueMap<String, String> body) {
		HttpHeaders header = makeHeader(mediaType, headers);
		return new HttpEntity<>(body, header);
	}
}
