package com.memo.common;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.memo.login.User;
import com.memo.login.repository.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
	private final UserDetailsService customUserDetailsService;
	private final RefreshTokenStore refreshTokenStore;
	private final  JwtProperties jwtProperties;
	private final UserRepository userRepository;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {

		CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();
		User user = userDetails.getUser();
		log.info("succcess handler의 userDetails: {}", userDetails.toString());
		JwtFilter jwtFilter = new JwtFilter(jwtProperties, customUserDetailsService, refreshTokenStore, userRepository);
		//성공필터에서 토큰 발급해주기,
		String accessToken = jwtFilter.create(user.getRole().name(), String.valueOf(user.getId()), Date.from(
			Instant.now().plus(3, ChronoUnit.HOURS)));
		String refreshToken = jwtFilter.create(user.getRole().name(), String.valueOf(user.getId()), Date.from(
			Instant.now().plus(7, ChronoUnit.DAYS)));
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
}
