package com.memo.login.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.memo.common.CustomOAuth2SuccessHandler;
import com.memo.common.CustomUserDetails;
import com.memo.common.RefreshTokenStore;
import com.memo.common.TokenProvider;
import com.memo.login.User;
import com.memo.login.oauth.CustomOAuthService;
import com.memo.login.oauth.kakao.KakaoApiClient;
import com.memo.login.repository.UserRepository;

import jakarta.servlet.http.Cookie;
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
	private final TokenProvider tokenProvider;
	private final RefreshTokenStore refreshTokenStore;


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

	public User oAuthLogin(String code, HttpServletResponse response) throws JsonProcessingException {
		User user = kakaoApiClient.oAuthLogin(code);

		customOAuthService.login(user);
		//인증된 객체 넣어주고
		setAuthentication(user); //굳이? 컨트롤러단에서 실행되는건데?
		setResponseToken(user, response);

		return user;
	}
	private void setResponseToken(User user, HttpServletResponse response) {


		// 토큰 발급해주기,
		String accessToken = tokenProvider.createAccessToken(user.getRole().name(), user.getId());
		String refreshToken = tokenProvider.createAccessToken(user.getRole().name(), user.getId());
		//리프레시 토큰은 스토리지에 저장
		refreshTokenStore.save(refreshToken, String.valueOf(user.getId()));

		//토큰 header에 넣어주기
		response.setHeader("Authorization", "Bearer " + accessToken);

		//리프레시는 http only 쿠키에
		Cookie cookie = new Cookie("refresh-token", refreshToken);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		cookie.setMaxAge(7 * 24 * 60 * 60); //리프레시 토큰도 1주일
		response.addCookie(cookie);
	}

	private void setAuthentication(User user) {
		UserDetails userDetails = CustomUserDetails.from(user);
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, user.getId(), userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}



}
