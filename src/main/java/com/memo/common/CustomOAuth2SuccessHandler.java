package com.memo.common;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.memo.login.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
	private final UserDetailsService customUserDetailsService;
	private final  JwtProperties jwtProperties;
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {

		CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();
		User user = userDetails.getUser();
		log.info("succcess handler의 userDetails: {}", userDetails.toString());
		JwtFilter jwtFilter = new JwtFilter(jwtProperties, customUserDetailsService);
		//성공필터에서 토큰 발급해주기,
		String token = jwtFilter.create(user.getRole().name(), String.valueOf(user.getId()));

		//토큰 header에 넣어주기
		response.setHeader("Authorization", token);
	}
}
