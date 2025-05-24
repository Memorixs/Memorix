package com.memo.login.oauth.kakao;

import static org.springframework.security.config.Elements.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.memo.common.Role;
import com.memo.login.User;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KakaoApiClient {
	private static final String LOGOUT = "https://kauth.kakao.com/oauth/logout";
	static final String authUrl = "https://kauth.kakao.com/oauth/authorize";
	private static final String tokenUrl = "https://kauth.kakao.com/oauth/token";
	private static final String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
	private static final String redirectUrl = "http://localhost:8080/login/oauth2/code/kakao";
	private static final String logoutRedirectUrl = "http://localhost:8080/logout/oauth2/kakao";
	private static final String tokenInfoURL = "https://kapi.kakao.com/v1/user/access_token_info";

	private ObjectMapper objectMapper;
	private RestTemplate restTemplate;
	private String clientId;

	public KakaoApiClient(@Value("${spring.security.oauth2.client.registration.kakao.client-id}") String clientId,
		ObjectMapper objectMapper, RestTemplate restTemplate ) {
		this.clientId = clientId;
		this.objectMapper = objectMapper;
		this.restTemplate = restTemplate;
	}



	public String authServerRequest() {
		//google api와 통신하기 위한 요청 만들기(승인 코드를 받기 위한 요청) //
		// authorizationCodeFlow.loadCredential(String);
		Map<String, String> params = new HashMap<>();
		params.put("client_id", clientId);
		params.put("redirect_uri", redirectUrl);
		params.put("response_type", "code");
		// params.put("scope", "profile_nickname");

		// params.put("state", createCSRFToken())
		String parameterString=params.entrySet().stream()
			.map(x->x.getKey()+"="+x.getValue())
			.collect(Collectors.joining("&"));
		String redirectURL=authUrl+"?"+parameterString;
		log.info("redirect-URL={}", redirectURL);
		return redirectURL;
	}

	public User oAuthLogin(String code) throws JsonProcessingException {
		KakaoTokenResponse oAuthResponse = requestAccessToken(code);
		//토큰 저장

		log.info("accessToken: {}", oAuthResponse.getAccessToken());
		// GoogleOAuthResponse googleOAuthToken =socialOauth.getAccessToken(accessToken);
		//
		KakaoInfo kakaoUser = requestUserInfo(oAuthResponse.getAccessToken());
		//
		//
		String user_id = kakaoUser.getId();
		//

		log.info("login user: {}", kakaoUser.toString());
		// return new GetSocialOAuthRes("1234",1,"asdf", googleOAuthToken.getToken_type());
		User user = User.from(kakaoUser);
		user.setRefreshToken(oAuthResponse.getRefreshToken());
		user.setAccessToken(oAuthResponse.getAccessToken());
		user.setRefreshTokenExpires(oAuthResponse.getRefreshTokenExpiresIn());
		user.setRole(Role.USER);


		return user;
	}

	public KakaoTokenResponse requestAccessToken(String code) {
		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("grant_type", "authorization_code");
		body.add("client_id", clientId);
		body.add("redirect_uri", redirectUrl);
		body.add("code", code);
		// client_secret이 필요한 경우 추가
		// body.add("client_secret", clientSecret);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

		ResponseEntity<KakaoTokenResponse> response = restTemplate.postForEntity(tokenUrl, request, KakaoTokenResponse.class);
		log.info("response: {}", response.toString());
		return response.getBody();
	}
	private KakaoTokenResponse getAccessToken(ResponseEntity<String> response) throws JsonProcessingException {
		log.info("accessTokenBody: {}",response.getBody());
		return objectMapper.readValue(response.getBody(), KakaoTokenResponse.class);
	}

	private KakaoInfo requestUserInfo(String token) {
		// lvLI6qji20bjm9IDpYyD3NJsJPTHEOQbAAAAAQoNFKMAAAGXAgKOYyn2EFsnJsRZ
		// HttpHeaders headers = new HttpHeaders();

		// HttpEntity<MultiValueMap<String,String>> request = new HttpEntity<>(headers);
		// headers.add("Authorization","Bearer "+ token);
		// ResponseEntity<String> exchange = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, String.class);
		// System.out.println(exchange.getBody());

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		httpHeaders.set("Authorization", "Bearer " + token);

		// Request entity 생성
		HttpEntity<?> userInfoEntity = new HttpEntity<>(httpHeaders);
		ResponseEntity<KakaoInfo> response = restTemplate.postForEntity(userInfoUrl, userInfoEntity, KakaoInfo.class);
		log.info("kakao userInfo: {}", response.toString());
		return response.getBody();
	}

	public void logout(String token, String blackListToken, User user) {
		int status = validateToken(token);
		if (status == 401) {
			String refreshToken = user.getRefreshToken();
			KakaoTokenResponse response = requestAccessTokenWithRefreshToken(refreshToken);
			token = response.getAccessToken();
		}
		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		// header.set("Authorization", "Bearer " + token);
		String url = LOGOUT + "?client_id=" + clientId +
			"&logout_redirect_uri=" + logoutRedirectUrl +
			"&state=" + blackListToken;

		ResponseEntity<Void> response = restTemplate.exchange(
			url,
			HttpMethod.GET,
			null,          // GET이므로 HttpEntity는 필요 없음
			Void.class
		);
		log.info(response.toString());

		} //logoutRedirectUrl로 리다이렉트

	private int validateToken(String token) {
		HttpHeaders header = new HttpHeaders();
		header.set("Authorization", "Bearer " + token);
		HttpEntity<?> request = new HttpEntity<>(header);
		ResponseEntity<KakaoTokenInfo> response = null;
		try {
			response = restTemplate.exchange(
				tokenInfoURL,           // 요청 URL
				HttpMethod.GET,         // HTTP 메서드
				request,                 // HttpEntity (헤더 포함)
				KakaoTokenInfo.class            // 응답을 매핑할 클래스
			);
		} catch(HttpClientErrorException e) {
			if(e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
				return HttpStatus.UNAUTHORIZED.value();
			}
		}

		log.info("token info: {}", response.toString());
		return response.getStatusCode().value();
	}

	private KakaoTokenResponse requestAccessTokenWithRefreshToken(String refreshToken) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);


		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("grant_type", "refresh_token");
		body.add("client_id", clientId);
		body.add("refresh_token", refreshToken);
		log.info("get new access Token: {}", body.toString());


		// Request entity 생성
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, httpHeaders);
		ResponseEntity<KakaoTokenResponse> response = restTemplate.postForEntity(tokenUrl, request, KakaoTokenResponse.class);
		log.info("토큰 갱신: {}", response.toString());
		return response.getBody();
	}
}
