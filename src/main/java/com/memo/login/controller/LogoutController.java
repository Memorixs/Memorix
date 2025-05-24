package com.memo.login.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.memo.common.CustomUserDetails;
import com.memo.common.RefreshTokenStore;
import com.memo.login.User;
import com.memo.login.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class LogoutController {
	private final UserService userService;
	private final RefreshTokenStore refreshTokenStore;

	//logout redirect url
	@GetMapping("/logout/oauth2/kakao")
	public ResponseEntity<String> logout(@RequestParam("state") Long id) {
		log.info("before logout Token Storage: {}", refreshTokenStore.toString());
		refreshTokenStore.remove(id);
		log.info("After logout Token Storage: {}", refreshTokenStore.toString());
		//브라우저 토큰 만료 -> 쿠키, 헤더 토큰삭제(프론트 역할), 디비에서 삭제
		return ResponseEntity.ok().body("ok");
	}

	//logout api 요청하면 위의 api로 리다이렉트된다. 이 api는 kakako 에서 발급해준 토큰을 만료시키는 api
	@PostMapping("/api/logout")
	public ResponseEntity<String> logoutKakao(@AuthenticationPrincipal CustomUserDetails userDetails){
		User user = userDetails.getUser();
		userService.logout(user);
		return ResponseEntity.ok().body("ok");
	}

}

