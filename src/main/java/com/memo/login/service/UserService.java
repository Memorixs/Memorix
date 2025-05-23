package com.memo.login.service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.memo.login.User;
import com.memo.login.oauth.CustomOAuthService;
import com.memo.login.oauth.kakao.KakaoApiClient;
import com.memo.login.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
	private static final String LOGOUT = "https://kapi.kakao.com/v1/user/logout";
	private final UserRepository userRepository;
	private final KakaoApiClient kakaoApiClient;
	private final CustomOAuthService customOAuthService;

	public String request(User user, HttpServletResponse response) {
		// oAuthTokenStore.get(user.getId())
		User findUser = userRepository.findById(user.getId())
			.orElseThrow(() -> new RuntimeException("회원정보가 존재하지 않습니다."));
		String accessToken = findUser.getAccessToken();
		response.setHeader("Authorization", "Bearer " + accessToken);

		//response body
		Map<String, String> params = new HashMap<>();
		params.put("user_id", "user_id");
		params.put("target_id", user.getProviderId());
		String parameterString=params.entrySet().stream()
			.map(x->x.getKey()+"="+x.getValue())
			.collect(Collectors.joining("&"));

		String redirectURL=LOGOUT+"?"+parameterString;
		log.info("redirect-URL={}", redirectURL);
		return redirectURL;
	}

	public User oAuthLogin(String code) throws JsonProcessingException {
		User user = kakaoApiClient.oAuthLogin(code);
		customOAuthService.login(user);
		return user;
	}
}
