package com.memo.user.controller;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.memo.user.DTO.LoginRequestDto;
import com.memo.user.DTO.UserRequestDto;
import com.memo.user.entity.User;
import com.memo.user.oauth.CustomOAuthService;
import com.memo.user.oauth.kakao.KakaoApiClient;
import com.memo.user.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
public class LoginController {

	private final KakaoApiClient kakaoApiClient;
	private final CustomOAuthService customOAuthService;
	private final UserService userService;

	@GetMapping("/test")
	public String test() {

		// log.info("인증 완료된 객체입니까? {}", SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
		// log.info("인증 완료된 객체입니까? {}", SecurityContextHolder.getContext().getAuthentication().toString());

		return "ok";
	}

	@ResponseBody
	@GetMapping("/test/oauth/login")
	public String testOAuthLogin(
		Authentication authentication,
		@AuthenticationPrincipal OAuth2User oauth
	) { //세션 정보 받아오기 (DI 의존성 주입)

		//방법 1
		OAuth2User oAuth2User = (OAuth2User)authentication.getPrincipal();
		System.out.println("authentication: " + oAuth2User.getAttributes());
		log.info("인증 완료된 객체입니까? {}", SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
		log.info("인증 완료된 객체입니까? {}", SecurityContextHolder.getContext().getAuthentication().toString());

		//방법 2
		System.out.println("OAuth2User:" + oauth.getAttributes());

		return "OAuth 세션 정보 확인";
	}

	@GetMapping("/login/oauth2")
	public void kakaoLogin(HttpServletResponse response) throws IOException {
		String requestURL = kakaoApiClient.authServerRequest(); //code 발급을 위한 요청
		response.sendRedirect(requestURL);
	}

	@GetMapping("/login/oauth2/code/kakao")
	public ResponseEntity<User> callback(@RequestParam(name = "code") String code, HttpServletResponse response) {
		log.info("code: {}", code);
		User user = userService.oAuthLogin(code, response);
		return new ResponseEntity<>(user, HttpStatus.OK);
	}

	@PostMapping("/api/login")
	public ResponseEntity<Long> login(HttpServletResponse response, @RequestBody LoginRequestDto requestDto) {
		Long id = userService.login(response, requestDto);
		return ResponseEntity.ok().body(id);
	}
}
