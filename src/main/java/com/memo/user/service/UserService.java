package com.memo.user.service;

import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.memo.common.security.CustomUserDetails;
import com.memo.common.jwt.RefreshTokenStore;
import com.memo.common.jwt.TokenProvider;
import com.memo.common.UtilString;
import com.memo.user.DTO.SignupFormRequestDto;
import com.memo.user.entity.User;
import com.memo.user.oauth.CustomOAuthService;
import com.memo.user.oauth.kakao.KakaoApiClient;
import com.memo.user.repository.UserRepository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
	private final CustomOAuthService customOAuthService;
	private final TokenProvider tokenProvider;
	private final RefreshTokenStore refreshTokenStore;
	private final KakaoApiClient kakaoApiClient;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public User oAuthLogin(String code, HttpServletResponse response)  {
		// User user = kakaoApiClient.oAuthLogin(code);
		User user = customOAuthService.login(code);
		//인증된 객체 넣어주고
		setAuthentication(user); //굳이? 컨트롤러단에서 실행되는건데?
		setResponseToken(user, response);

		return user;
	}
	private void setResponseToken(User user, HttpServletResponse response) {
		// 토큰 발급해주기,
		String accessToken = tokenProvider.createAccessToken(user.getRole().name(), user.getId());
		String refreshToken = tokenProvider.createAccessToken(user.getRole().name(), user.getId());
		//리프레시 토큰은 스토리지에 저장
		refreshTokenStore.save(user.getId(), refreshToken);

		//토큰 header에 넣어주기
		response.setHeader(UtilString.AUTHORIZATION.value(), UtilString.BEARER.value() + accessToken);

		//리프레시는 http only 쿠키에
		Cookie cookie = createCookie(refreshToken);
		response.addCookie(cookie);
	}

	private static Cookie createCookie(String refreshToken) {
		Cookie cookie = new Cookie(UtilString.COOKIE_NAME.value(), refreshToken);
		cookie.setPath("/api");
		cookie.setHttpOnly(true);
		cookie.setMaxAge(7 * 24 * 60 * 60); //리프레시 토큰도 1주일
		return cookie;
	}

	private void setAuthentication(User user) {
		UserDetails userDetails = CustomUserDetails.from(user);
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, user.getId(), userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	//카카오 로그아웃
	public void logout(HttpServletRequest request, User user) {
		//엑세스토큰으로 요청
		String token = request.getHeader(UtilString.AUTHORIZATION.value());
		String jwt = TokenProvider.resolveToken(token);
		String accessToken = user.getAccessToken();
		kakaoApiClient.logout(accessToken, jwt, user);
	}

	public void deleteCookie(HttpServletResponse response) {

		ResponseCookie deleteCookie = ResponseCookie.from(UtilString.COOKIE_NAME.value(), "")
			.path("/")
			.httpOnly(false)
			.secure(true)
			.sameSite("None")
			.maxAge(0)
			.build();

		response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
	}

	@Transactional
	public User signup(SignupFormRequestDto requestDto) {
		//이메일 인증 -> 임시번호
		//인증했다치고
		Optional<User> findUserByEmail = Optional.ofNullable(userRepository.findByEmailEquals(requestDto.getEmail()));
		Optional<User> findUserByUsername  = Optional.ofNullable(userRepository.findByUsernameEquals(requestDto.getUsername()));

		if(findUserByEmail.isPresent()) {
			throw new RuntimeException("중복되는 이메일입니다.");
		} else if (findUserByUsername.isPresent()) {
			throw new RuntimeException("중복되는 username 입니다.");
		}
		//회원가입 진행
		String encodedPw = passwordEncoder.encode(requestDto.getPassword());
		User user = User.of(requestDto, encodedPw);
		return userRepository.save(user);
	}
}
