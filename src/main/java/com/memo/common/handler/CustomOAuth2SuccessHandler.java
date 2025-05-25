package com.memo.common.handler;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.memo.common.security.CustomUserDetails;
import com.memo.common.jwt.JwtFilter;
import com.memo.common.jwt.RefreshTokenStore;
import com.memo.common.jwt.TokenBlackListStore;
import com.memo.common.jwt.TokenProvider;
import com.memo.common.util.UtilString;
import com.memo.user.entity.User;

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
	private final RefreshTokenStore refreshTokenStore;
	private final TokenBlackListStore tokenBlackListStore;
	private final TokenProvider tokenProvider;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {

		CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();
		User user = userDetails.getUser();
		log.info("succcess handler의 userDetails: {}", userDetails.toString());
		JwtFilter jwtFilter = new JwtFilter(refreshTokenStore, tokenBlackListStore, tokenProvider);

		//성공필터에서 토큰 발급해주기,
		String accessToken = tokenProvider.create(user.getRole().name(), String.valueOf(user.getId()), Date.from(
			Instant.now().plus(3, ChronoUnit.HOURS)));
		String refreshToken = tokenProvider.create(user.getRole().name(), String.valueOf(user.getId()), Date.from(
			Instant.now().plus(7, ChronoUnit.DAYS)));
		//리프레시 토큰은 스토리지에 저장
		refreshTokenStore.save(user.getId(), refreshToken);

		//토큰 header에 넣어주기
		response.setHeader(UtilString.AUTHORIZATION.value(), UtilString.BEARER.value() + accessToken);

		//리프레시는 http only 쿠키에
		Cookie cookie = new Cookie(UtilString.COOKIE_NAME.value(), refreshToken);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		cookie.setMaxAge(7 * 24 * 60 * 60); //리프레시 토큰도 1주일
		response.addCookie(cookie);
		response.sendRedirect("/");
	}
}
