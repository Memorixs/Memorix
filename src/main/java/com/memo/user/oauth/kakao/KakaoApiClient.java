package com.memo.user.oauth.kakao;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.memo.common.properties.OAuth2ClientProperties;
import com.memo.storage.TokenRepository;
import com.memo.user.entity.Role;
import com.memo.user.entity.User;
import com.memo.user.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KakaoApiClient {
	private final String authUrl;
	private final String tokenUrl;
	private final String userInfoUrl;
	private final String redirectUrl;

	private final ObjectMapper objectMapper;
	private final RestTemplate restTemplate;
	private final String clientId;
	private final TokenRepository tokenRepository;
	private final UserRepository userRepository;

	public KakaoApiClient(OAuth2ClientProperties oAuth2ClientProperties, ObjectMapper objectMapper, RestTemplate restTemplate,
		TokenRepository tokenRepository, UserRepository userRepository) {
		this.clientId = oAuth2ClientProperties.getRegistration().get("kakao").getClientId();
		this.tokenUrl = oAuth2ClientProperties.getProvider().get("kakao").getTokenUri();
		this.redirectUrl = oAuth2ClientProperties.getRegistration().get("kakao").getRedirectUri();
		this.userInfoUrl = oAuth2ClientProperties.getProvider().get("kakao").getUserInfoUri();
		this.authUrl = oAuth2ClientProperties.getProvider().get("kakao").getAuthorizationUri();
		this.objectMapper = objectMapper;
		this.restTemplate = restTemplate;
		this.tokenRepository = tokenRepository;
		this.userRepository = userRepository;
	}



	public String authServerRequest() {
		//google api와 통신하기 위한 요청 만들기(승인 코드를 받기 위한 요청) //
		Map<String, String> params = new HashMap<>();
		params.put("client_id", clientId);
		params.put("redirect_uri", redirectUrl);
		params.put("response_type", "code");

		String parameterString=params.entrySet().stream()
			.map(x->x.getKey()+"="+x.getValue())
			.collect(Collectors.joining("&"));
		String redirectURL=authUrl+"?"+parameterString;
		log.info("redirect-URL={}", redirectURL);
		return redirectURL;
	}

	public User createUser(KakaoTokenResponse token) { //매번 로그인할 때마다 거쳐야함
		KakaoInfo kakaoUser = requestUserInfo(token.getAccessToken());
		log.info("login user: {}", kakaoUser.toString());

		User user = User.from(kakaoUser);
		User newUser = userRepository.findByEmail(user.getEmail())
			.orElseGet(() -> userRepository.save(user));
		newUser.setRole(Role.USER);

		return newUser;
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

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		httpHeaders.set("Authorization", "Bearer " + token);

		// Request entity 생성
		HttpEntity<?> userInfoEntity = new HttpEntity<>(httpHeaders);
		ResponseEntity<KakaoInfo> response = restTemplate.postForEntity(userInfoUrl, userInfoEntity, KakaoInfo.class);
		return response.getBody();
	}

	public void logout(String token, User user) {
		String logout = "https://kapi.kakao.com/v1/user/logout";
		int status = validateToken(token);
		if (status == 401) {
			String refreshToken = tokenRepository.findByKey("kakaoRefresh;id"+user.getId());

			KakaoTokenResponse response = requestAccessTokenWithRefreshToken(refreshToken);
			token = response.getAccessToken();
		}

		HttpHeaders header = new HttpHeaders();
		header.setBearerAuth(token);
		header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		HttpEntity<?> requestLogout = new HttpEntity<>(header);

		header.set("Authorization", "Bearer " + token);
		ResponseEntity<KakaoTokenInfo> response = restTemplate.postForEntity(logout, requestLogout, KakaoTokenInfo.class);

		log.info(response.toString());

		}

	private int validateToken(String token) {
		HttpHeaders header = new HttpHeaders();
		header.set("Authorization", "Bearer " + token);
		HttpEntity<?> request = new HttpEntity<>(header);
		ResponseEntity<KakaoTokenInfo> response = null;
		try {
			String tokenInfoUrl = "https://kapi.kakao.com/v1/user/access_token_info";
			response = restTemplate.exchange(
				tokenInfoUrl,           // 요청 URL
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
		return response.getBody();
	}
}
