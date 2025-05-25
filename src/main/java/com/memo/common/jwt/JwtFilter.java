package com.memo.common.jwt;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.springframework.web.filter.OncePerRequestFilter;

import com.memo.common.UtilString;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtFilter extends OncePerRequestFilter {
	//리프레시 토큰 저장할때가 로그인할때임
	private final RefreshTokenStore refreshTokenStore;
	private final TokenBlackListStore tokenBlackListStore;
	private final String[] permitList = {"/login","/logout/oauth2/kakao", "/webjars", "/.well-known"};
	private final TokenProvider tokenProvider;

	public JwtFilter(RefreshTokenStore refreshTokenStore, TokenBlackListStore tokenBlackListStore,
		TokenProvider tokenProvider) {

		this.refreshTokenStore = refreshTokenStore;
		this.tokenBlackListStore = tokenBlackListStore;
		this.tokenProvider = tokenProvider;
	}
	//공식문서에 Filter를 구현하기보다	 OncePerRequestFilter 를 확장하라고 되어 있다. -> 각 요청당 한번만 invoke된다. 그리고 dofilterInternal이 HttpServletRequest HttpServletResponse 제공
	//Filter는 그냥 ServletRequest 을 제공
	// https://docs.spring.io/spring-security/reference/servlet/architecture.html#_adding_a_custom_filter
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		//인증이 필요하지 않은 요청
		String requestUri = request.getRequestURI();
		log.info("request URI: {}", request.getRequestURI());
		for(String uri:permitList) {
			if (requestUri.equals("/")) {
				filterChain.doFilter(request, response);
			}
			if (requestUri.contains(uri)) {
				filterChain.doFilter(request, response);
				return;
			}
		}
		//토큰 꺼내기
		String jwt = null;
		jwt = request.getHeader(UtilString.AUTHORIZATION.value());
		log.info("jwt: {}", jwt);
		if (jwt == null || !jwt.startsWith(UtilString.BEARER.value())) {
			if (jwt == null) throw new RuntimeException("AUTHORIZATION 헤더 값이 비었습니다.");
			if(!jwt.startsWith(UtilString.BEARER.value())) throw new RuntimeException("토큰 값은 \"Bearer\" 로 시작해야합니다.");
		}
		// String token = jwt.replace(HEADER_STRING, "");
		String token = TokenProvider.resolveToken(jwt);
		//블랙리스트에 등록된 토큰인지 확인
		log.info("블랙리스트에 등록된 토큰인가? {}", tokenBlackListStore.contains(token));
		if(tokenBlackListStore.contains(token)) {
			throw new RuntimeException("로그아웃된 회원입니다.");
		}

		log.info("get AccessToken from header: {}", token);

		//해시알고리즘으로 signature 암호화
		String tokenValid = tokenProvider.validate(token); //id로 사용자 가져와서 securitycontextholder에 적재?


		if (tokenValid.equals("EXPIRED")) {
			//만료된 토큰은 리프레시토큰을 활용해서 토큰 발급
			//요청마다 securityContextHolder는 clear된다

			for(Cookie cookie : request.getCookies()) {
				if (cookie.getName().equals(UtilString.COOKIE_NAME.value())) {
					String refreshToken = cookie.getValue();
					tokenValid = tokenProvider.validate(refreshToken);

					//엑세스 토큰과 리프레시 모두 만료
					if (tokenValid.equals("EXPIRED")) {
						//예외: 재로그인
						throw new RuntimeException("토큰이 모두 만료되었습니다. 재로그인 해주세요.");
					}
					Long userId = Long.valueOf(tokenValid);
					if(!refreshTokenStore.containsKey(userId)) {
						throw new RuntimeException("리프레시 토큰이 존재하지 않아 토큰 갱신이 불가능합니다.");
					}
					String findRefreshToken = refreshTokenStore.get(userId);

					String accessToken = createAccessToken(findRefreshToken, userId);
					response.setHeader(UtilString.AUTHORIZATION.value(), accessToken);
				}
			}
		}
		tokenProvider.setAuthentication(tokenValid);
		//다음 필터
		filterChain.doFilter(request, response);
	}

	private String createAccessToken(String refreshToken, Long userId) {
		String role = (String)tokenProvider.getClaims(refreshToken).get("role"); //role을 가지고 오기위해 디비에 접근하기보다 리프레시 토큰안에 있는 정보 활용
		String accessToken = tokenProvider.create(role, userId,
			Date.from(Instant.now().plus(3, ChronoUnit.HOURS)));

		return accessToken;
		}

}
