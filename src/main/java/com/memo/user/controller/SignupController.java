package com.memo.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.memo.user.DTO.SignupFormRequestDto;
import com.memo.user.entity.User;
import com.memo.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

@Tag(name = "User API", description = "유저 도메인 API")
@RestController
@RequiredArgsConstructor
public class SignupController {
	private final UserService userService;

	@Operation(
		summary = "회원가입",
		description = "Body로 json형식의 email, username, password를 보내서 회원가입",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "회원가입 JSON Body 데이터",
			required = true,
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = SignupFormRequestDto.class)
			)
		),
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "성공",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(implementation = User.class)
				)
			),
			@ApiResponse(
				responseCode = "400",
				description = "실패",
				content = @Content(
					examples = @ExampleObject(
						value = "실패하였습니다."
					)
				)
			),
			@ApiResponse(
				responseCode = "500",
				description = "서버 오류",
				content = @Content(
					examples = @ExampleObject(
						value = "실패하였습니다."
					)
				)
			)

		}
	)
	@PostMapping("/api/signup")
	public ResponseEntity<User> signup(@RequestBody SignupFormRequestDto requestDto) throws MessagingException {
		User user = userService.signup(requestDto);
		return ResponseEntity.ok().body(user);
	}

	@Operation(
		summary = "이메일 인증",
		description = "사용자가 만료된 이메일 링크를 클릭했을 때 호출",
		parameters = {
			@Parameter(
				name = "email",
				description = "인증 링크 보낼 이메일",
				required = true,
				in = ParameterIn.QUERY,
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(implementation = User.class)
				)
			)
		},
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "성공",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(implementation = User.class)
				)
			),
			@ApiResponse(
				responseCode = "400",
				description = "실패",
				content = @Content(
					examples = @ExampleObject(
						value = "실패하였습니다."
					)
				)
			),
			@ApiResponse(
				responseCode = "500",
				description = "서버 오류",
				content = @Content(
				examples = @ExampleObject(
					value = "실패하였습니다."
				)
			)
			)
		}
	)
	//이메일 인증 -> 리다이렉트
	@GetMapping("/api/auth/confirm")
	public ResponseEntity<User> confirmEmail(@RequestParam String email) throws MessagingException {
		//토큰 검증(만료되었는지 아닌지, 만료되면 재전송
		User user = userService.verifiedUser(email);
		return ResponseEntity.ok().body(user);
	}
}
