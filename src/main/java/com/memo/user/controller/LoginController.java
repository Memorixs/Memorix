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

import com.memo.user.DTO.LoginRequestDto;

import com.memo.user.entity.User;
import com.memo.user.oauth.CustomOAuthService;
import com.memo.user.oauth.kakao.KakaoApiClient;
import com.memo.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "User API", description = "유저 도메인 API")
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

	@Operation(
		summary = "카카오 간편 로그인",
		description = "카카오 간편 로그인 시 호출",
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
				description = "실패"
			),
			@ApiResponse(
				responseCode = "500",
				description = "서버 오류"
			)
		}
	)
	@GetMapping("/login/oauth2")
	public void kakaoLogin(HttpServletResponse response) throws IOException {
		String requestURL = kakaoApiClient.authServerRequest(); //code 발급을 위한 요청
		response.sendRedirect(requestURL);
	}

	@Operation(
		summary = "카카오 토큰 발급을 위한 리다이렉트 API",
		description = "쿼리 파라미터로 code 값 입력",
		parameters = {
			@Parameter(
				name = "code",
				description = "카카오 서버에게 토큰 발급을 위해 필요한 code",
				required = true,
				in = ParameterIn.QUERY
			)
		},
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "성공"
			),
			@ApiResponse(
				responseCode = "400",
				description = "실패"
			)
		}
	)
	@GetMapping("/login/oauth2/code/kakao")
	public ResponseEntity<Long> callback(@RequestParam(name = "code") String code, HttpServletResponse response) {
		log.info("code: {}", code);
		User user = userService.oAuthLogin(code, response);
		return new ResponseEntity<>(user.getId(), HttpStatus.OK);
	}

	@Operation(
		summary = "서비스 로그인",
		description = "서비스 로그인 API",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody (
			description = "로그인 JSON body 데이터",
			required = true,
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = LoginRequestDto.class)
			)
		),
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "성공"
			),
			@ApiResponse(
				responseCode = "400",
				description = "실패",
				content = @Content(
					examples = @ExampleObject(
						value = "로그인에 실패하였습니다."
					)
				)
			)
		}
	)
	@PostMapping("/api/login")
	public ResponseEntity<Long> login(HttpServletResponse response, @RequestBody LoginRequestDto requestDto) {
		Long id = userService.login(response, requestDto);
		return ResponseEntity.ok().body(id);
	}
}
