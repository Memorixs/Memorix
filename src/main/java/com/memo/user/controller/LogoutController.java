package com.memo.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.memo.common.security.CustomUserDetails;
import com.memo.common.jwt.TokenProvider;
import com.memo.storage.RefreshTokenRepository;
import com.memo.storage.TokenBlackList;
import com.memo.storage.TokenBlackListRepository;
import com.memo.user.entity.User;
import com.memo.user.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class LogoutController {
	private final UserService userService;
	private final RefreshTokenRepository refreshTokenStore;
	private final TokenBlackListRepository tokenBlackListStore;
	private final TokenProvider tokenProvider;


	//logout redirect url
// /logout/oauth2/kakao
	@GetMapping("/logout/oauth2/kakao")
	public ResponseEntity<String> logout(@RequestParam("state") String token, HttpServletResponse response) {
		//id는 서버끼리 통신이라 탈취되지 않을 것?
		log.info("logout token: {}", token);
		userService.logout(token, response);
		return ResponseEntity.ok().body("ok");
	}

	//logout api 요청하면 위의 api로 리다이렉트된다. 이 api는 kakako 에서 발급해준 토큰을 만료시키는 api
	@PostMapping("/api/logout")
	public ResponseEntity<String> logoutKakao(HttpServletRequest request, @AuthenticationPrincipal CustomUserDetails userDetails){
		User user = userDetails.getUser();
		userService.logout(request, user);
		return ResponseEntity.ok().body("ok");
	}

}

