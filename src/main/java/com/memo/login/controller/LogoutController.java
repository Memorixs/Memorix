package com.memo.login.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.memo.common.CustomUserDetails;
import com.memo.login.User;
import com.memo.login.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class LogoutController {
	private final UserService userService;

	@PostMapping("/api/logout")
	public void logout(HttpServletResponse response, Authentication authentication) throws IOException {
		CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();
		User user = userDetails.getUser();
		String requestUrl = userService.request(user, response);
		response.sendRedirect(requestUrl);
	}

	//logout redirect url
	@PostMapping("/logout/oauth2/kakao")
	public ResponseEntity<String> logout(){
		// userService.logout();
		return ResponseEntity.ok().body("ok");
	}

}

