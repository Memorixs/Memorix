package com.memo.login.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.memo.common.CustomUserDetails;
import com.memo.login.User;
import com.memo.login.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class LogoutController {
	private final UserService userService;

	//logout redirect url
	// @PostMapping("/logout/oauth2/kakao")
	// public void logout(HttpServletResponse response, Authentication authentication) throws IOException {
	// 	CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();
	// 	User user = userDetails.getUser();
	// 	String requestUrl = userService.request(user, response);
	// 	response.sendRedirect(requestUrl);
	// }


	//logout api 요청하면 위의 api로 리다이렉트된다. 이 api는 kakako 에서 발급해준 토큰을 만료시키는 api
	@PostMapping("/api/logout")
	public ResponseEntity<String> logout(@AuthenticationPrincipal CustomUserDetails userDetails){
		User user = userDetails.getUser();
		log.info("find user in AuthenticationPrincipal: {}", user.toString());
		userService.logout(user);
		return ResponseEntity.ok().body("ok");
	}

}

