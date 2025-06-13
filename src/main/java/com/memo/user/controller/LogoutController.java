package com.memo.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.memo.common.security.CustomUserDetails;
import com.memo.user.entity.User;
import com.memo.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "User API", description = "유저 도메인 API")
@RestController
@RequiredArgsConstructor
@Slf4j
public class LogoutController {
	private final UserService userService;

	//logout redirect url
// /logout/oauth2/kakao
	@GetMapping("/logout/oauth2/kakao")
	public ResponseEntity<String> logout(@RequestParam("state") String token, HttpServletResponse response) {
		//id는 서버끼리 통신이라 탈취되지 않을 것?
		userService.logout(token);
		userService.deleteCookie(response);
		return ResponseEntity.ok().body("ok");
	}

	//logout api 요청하면 위의 api로 리다이렉트된다. 이 api는 kakako 에서 발급해준 토큰을 만료시키는 api
	@Operation(
		summary = "카카오 로그아웃",
		description = "카카오 로그아웃 시 호출",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "성공"

			),
			@ApiResponse(
				responseCode = "400",
				description = "실패"
			),
			@ApiResponse(
				responseCode = "500",
				description = "서버 오류"
			)
		}
	)
	@PostMapping("/api/logout")
	public ResponseEntity<String> logoutKakao(HttpServletRequest request, @AuthenticationPrincipal CustomUserDetails userDetails){
		User user = userDetails.getUser();
		userService.logout(request, user);
		return ResponseEntity.ok().body("ok");
	}

}

