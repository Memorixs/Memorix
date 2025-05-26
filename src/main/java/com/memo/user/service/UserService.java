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
import com.memo.common.jwt.TokenProvider;
import com.memo.common.util.CustomPasswordEncoder;
import com.memo.common.util.EmailService;
import com.memo.common.util.UtilString;
import com.memo.storage.RefreshToken;
import com.memo.storage.RefreshTokenRepository;
import com.memo.storage.TokenBlackList;
import com.memo.storage.TokenBlackListRepository;
import com.memo.user.DTO.UserRequestDto;
import com.memo.user.entity.LoginType;
import com.memo.user.entity.User;
import com.memo.user.oauth.CustomOAuthService;
import com.memo.user.oauth.kakao.KakaoApiClient;
import com.memo.user.repository.UserRepository;

import jakarta.mail.MessagingException;
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
	private final RefreshTokenRepository refreshTokenStore;
	private final KakaoApiClient kakaoApiClient;
	private final UserRepository userRepository;
	private final CustomPasswordEncoder passwordEncoder;
	private final EmailService signupEmailService;
	private final TokenBlackListRepository tokenBlackListStore;

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
		refreshTokenStore.save(new RefreshToken(user.getId(), refreshToken));

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
	@Transactional
	public void logout(HttpServletRequest request, User user) {
		//엑세스토큰으로 요청
		String token = request.getHeader(UtilString.AUTHORIZATION.value()); //서비스 토큰
		String jwt = TokenProvider.resolveToken(token);
		String accessToken = user.getAccessToken();
		switch(user.getLoginType()) {
			case NATIVE -> logout(jwt);
			case KAKAO -> kakaoApiClient.logout(accessToken, jwt, user);
		}
	}

	@Transactional
	public void logout(String token) {
		String stringId = tokenProvider.getClaims(token).getSubject();
		refreshTokenStore.deleteByUserId(Long.valueOf(stringId));
		//브라우저 토큰 만료 -> 쿠키, 헤더 토큰삭제(프론트 역할), 디비에서 삭제

		tokenBlackListStore.save(new TokenBlackList(token));
		//리프레시 쿠키 삭제하도록 응답
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
	public User signup(UserRequestDto requestDto) throws MessagingException {
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
		//1. 확인 이메일 전송
		signupEmailService.sendVerifiedMessage(requestDto.getEmail()); ///api/auth/confirm"로 리다이렉트 -> 회원이 영원히 누르지 않으면 여기서 멈출듯
//메일 전송 후 레디스에 email + 만료시간 설정 sendVerifiedMessage내에서 실행
		String password = getEncodedPassword(requestDto.getPassword());
		User user = User.of(requestDto, password);
		return userRepository.save(user);
	}

	private String getEncodedPassword(String password) {
		PasswordEncoder encoder = passwordEncoder.passwordEncoder();
		String encodedPw = encoder.encode(password);
		return encodedPw;
	}


	@Transactional
	public User verifiedUser(String token) throws MessagingException {
		//1. 토큰 검증 -> 만료면 다시 메일 보내기, 아니면 인증 성공 후 회원가입 진행
		User user = signupEmailService.validateEmailToken(token);
		Optional.ofNullable(user)
			.orElseThrow(() -> new RuntimeException("토큰 확인 과정 중 문제가 발생했습니다."));
		//미리 회원을 저장하고 인증이 완료되면 isVerified를 true로 변경
		user.setIsVerified(true);
		return user;
	}

	public Long login(HttpServletResponse response, UserRequestDto requestDto) {
		User user = userRepository.findByEmail(requestDto.getEmail())
			.orElseThrow(() -> new RuntimeException("가입된 정보가 없습니다. email: "+ requestDto.getEmail()));
		if (!isMatchesPassword(requestDto.getPassword(), user.getPassword())){
			throw new RuntimeException("비밀번호가 일치하지 않습니다. password: " + requestDto.getPassword());
		}
		if(!user.getIsVerified()) {
			throw new RuntimeException("검증되지 않은 사용자입니다. verified: " + user.getIsVerified());
		}
		//토큰 생성
		setResponseToken(user, response);
		return user.getId();
	}

	private boolean isMatchesPassword(String rawPassword, String encodedPassword){
		PasswordEncoder endcoder = passwordEncoder.passwordEncoder();
		return endcoder.matches(rawPassword, encodedPassword);
	}
}
