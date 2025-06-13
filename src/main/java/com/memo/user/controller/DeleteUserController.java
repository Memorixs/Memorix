package com.memo.user.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

import com.memo.common.security.CustomUserDetails;
import com.memo.user.entity.User;
import com.memo.user.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DeleteUserController {

	private final UserService userService;

	@DeleteMapping("/api/users")
	public void deleteUser(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletRequest request, HttpServletResponse response) {
		//카카오 로그인 사용자인지 확인) {
		//토큰 삭제, 유저 soft delete, 카카오 로그인 사용자인지 확인, 카카오면
		User user = userDetails.getUser();
		userService.deleteUser(user, request, response);
	}

}
