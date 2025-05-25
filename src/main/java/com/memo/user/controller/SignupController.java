package com.memo.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.memo.common.security.CustomUserDetails;
import com.memo.user.DTO.SignupFormRequestDto;
import com.memo.user.entity.User;
import com.memo.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SignupController {
	private final UserService userService;

	@PostMapping("/api/signup")
	public ResponseEntity<Long> signup(@RequestBody SignupFormRequestDto requestDto) {
		User user = userService.signup(requestDto);
		return ResponseEntity.ok().body(user.getId());
	}
}
