package com.memo.user.oauth.kakao;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.memo.common.util.UtilString;
import com.memo.storage.TokenRepository;
import com.memo.user.entity.Role;
import com.memo.user.entity.User;
import com.memo.user.oauth.OAuthApiClient;
import com.memo.user.oauth.OAuthUserResponse;
import com.memo.user.oauth.TokenResponse;
import com.memo.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class KakaoApiClient implements OAuthApiClient {

	private final RestTemplate restTemplate;
	private final TokenRepository tokenRepository;
	private final UserRepository userRepository;
	private final KakaoRequest kakaoRequest;
	@Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
	private String tokenUrl;
	@Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
	private String userInfoUrl;


	public User createUser(TokenResponse token) { //매번 로그인할 때마다 거쳐야함
		OAuthUserResponse kakaoUser = requestOAuthUser(token.getAccessToken());
		log.info("login user: {}", kakaoUser.toString());

		User user = User.from(kakaoUser);
		User newUser = userRepository.findByEmail(user.getEmail())
			.orElseGet(() -> userRepository.save(user));
		newUser.setRole(Role.USER);

		return newUser;
	}

	public User login(String code){
		TokenResponse response = requestAccessToken(code);
		User user = createUser(response);
		tokenRepository.save("kakaoAccess;id" + user.getId(), response.getAccessToken(), response.getExpiresIn(), TimeUnit.SECONDS);
		tokenRepository.save("kakaoRefresh;id" + user.getId(), response.getRefreshToken(), response.getRefreshTokenExpiresIn(), TimeUnit.SECONDS);
		return user;
	}

	public TokenResponse requestAccessToken(String code) {
		RestTemplate restTemplate = new RestTemplate();

		// HttpHeaders headers = new HttpHeaders();
		// headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> body = kakaoRequest.makeBody(code);
		HttpEntity request = KakaoRequest.makeRequestEntity(MediaType.APPLICATION_FORM_URLENCODED, null, body);
		// MultiValueMap<String, String> body = kakaoLoginParams.makebody(code);


		// HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

		//HttpEntity(body), HttpEntity(body,header)
		ResponseEntity<KakaoTokenResponse> response = restTemplate.postForEntity(tokenUrl, request, KakaoTokenResponse.class);
		log.info("response: {}", response.toString());
		return response.getBody();
	}
	// private KakaoTokenResponse getAccessToken(ResponseEntity<String> response) throws JsonProcessingException {
	// 	log.info("accessTokenBody: {}",response.getBody());
	// 	return objectMapper.readValue(response.getBody(), KakaoTokenResponse.class);
	// }

	@Override
	public OAuthUserResponse requestOAuthUser(String token) {


		Map<String, String> header = new HashMap<>();
		header.put(UtilString.AUTHORIZATION.value(), UtilString.BEARER.value() + token);
		HttpEntity request = KakaoRequest.makeRequestEntity(MediaType.APPLICATION_FORM_URLENCODED, header, null);

		// Request entity 생성
		ResponseEntity<KakaoUserResponse> response = restTemplate.postForEntity(userInfoUrl, request, KakaoUserResponse.class);
		return response.getBody();
	}


	public void logout(User user) {
		Long userId = user.getId();
		String accessToken = tokenRepository.findByKey(userId + UtilString.KAKAO_ACCESS_TOKEN.value());
		String logout = "https://kapi.kakao.com/v1/user/logout";
		int status = validateToken(accessToken);
		if (status == 401) {
			String refreshToken = tokenRepository.findByKey(userId + UtilString.KAKAO_REFRESH_TOKEN.value());

			KakaoTokenResponse response = requestAccessTokenWithRefreshToken(refreshToken);
			accessToken = response.getAccessToken();
		}

		// HttpHeaders header = new HttpHeaders();
		// header.setBearerAuth(accessToken);
		// header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		//
		// HttpEntity<?> requestLogout = new HttpEntity<>(header);
		//
		// header.set("Authorization", "Bearer " + accessToken);
		Map<String, String> header = new HashMap<>();
		header.put(UtilString.AUTHORIZATION.value(), UtilString.BEARER.value() + accessToken);
		HttpEntity request = KakaoRequest.makeRequestEntity(MediaType.APPLICATION_FORM_URLENCODED, header, null);

		ResponseEntity<KakaoTokenInfo> response = restTemplate.postForEntity(logout, request, KakaoTokenInfo.class);

		log.info(response.toString());
		tokenRepository.deleteByKey(userId + UtilString.KAKAO_ACCESS_TOKEN.value());
		tokenRepository.deleteByKey(userId + UtilString.SERVICE_REFRESH_TOKEN.value());

		}

	private int validateToken(String token) {

		Map<String, String> addHeader = new HashMap<>();
		addHeader.put(UtilString.AUTHORIZATION.value(), UtilString.BEARER.value() + token);
		HttpEntity request = KakaoRequest.makeRequestEntity(null, addHeader, null);


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

		HttpHeaders header = kakaoRequest.makeHeader(MediaType.APPLICATION_FORM_URLENCODED, null);
		MultiValueMap<String, String> body = kakaoRequest.makeBody("refresh_token", refreshToken);


		// Request entity 생성
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, header);
		ResponseEntity<KakaoTokenResponse> response = restTemplate.postForEntity(tokenUrl, request, KakaoTokenResponse.class);
		return response.getBody();
	}
}
