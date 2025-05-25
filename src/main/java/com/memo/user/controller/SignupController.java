package com.memo.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.memo.common.security.CustomUserDetails;
import com.memo.user.DTO.SignupFormRequestDto;
import com.memo.user.entity.User;
import com.memo.user.service.UserService;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SignupController {
	private final UserService userService;

	@PostMapping("/api/signup")
	public ResponseEntity<User> signup(@RequestBody SignupFormRequestDto requestDto) throws MessagingException {
		User user = userService.signup(requestDto);
		return ResponseEntity.ok().body(user);
	}

	//이메일 인증 -> 리다이렉트
	@GetMapping("/api/auth/confirm")
	public ResponseEntity<User> confirmEmail(@RequestParam String email) throws MessagingException {
		//토큰 검증(만료되었는지 아닌지, 만료되면 재전송
		User user = userService.verifiedUser(email);
		return ResponseEntity.ok().body(user);
	}
}
