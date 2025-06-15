package com.memo.common.jwt;

import java.io.IOException;

import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.memo.common.exception.CustomException;
import com.memo.common.exception.TokenExceptionType;
import com.memo.common.util.UtilString;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

	private final String[] permitList = {"/login","/logout/oauth2/kakao", "/webjars", "/.well-known", "/api/signup","/api/auth/confirm", "/favicon.ico", "/api-docs", "/swagger-ui"};
	private final TokenProvider tokenProvider;



	//공식문서에 Filter를 구현하기보다	 OncePerRequestFilter 를 확장하라고 되어 있다. -> 각 요청당 한번만 invoke된다. 그리고 dofilterInternal이 HttpServletRequest HttpServletResponse 제공
	//Filter는 그냥 ServletRequest 을 제공
	// https://docs.spring.io/spring-security/reference/servlet/architecture.html#_adding_a_custom_filter
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		//인증이 필요하지 않은 요청
		ignoreRequest(request, response, filterChain);

		//토큰 헤더에서 꺼내기
		String token = resolveTokenFromRequest(request);
		//해시알고리즘으로 signature 암호화

		try{
			String tokenValid = tokenProvider.validate(token); //id로 사용자 가져와서 securitycontextholder에 적재?
			tokenProvider.isBlackListUser(token, tokenValid); //블랙리스트에 들어가는 토큰은 액세스 토큰이므로 엑세스 토큰 만료시간 만큼 timeout을 지정?
		} catch (CustomException e) {
			if(e.getCode() == 4001) { //엑세스 토큰 만료
				String refreshToken = getTokenFromCookie(request);

				//리프레시 토큰으로 엑세스 토큰 갱신
				token = tokenProvider.renewalAccessToken(refreshToken);
				response.setHeader(UtilString.AUTHORIZATION.value(), token);
			}
		}
		tokenProvider.setAuthentication(token);

		//다음 필터
		filterChain.doFilter(request, response);
	}

	private String resolveTokenFromRequest(HttpServletRequest request) {
		String jwt = null;
		jwt = request.getHeader(UtilString.AUTHORIZATION.value());
		log.info("jwt: {}", jwt);

		if (jwt == null) {
			throw new CustomException(TokenExceptionType.EMPTY_AUTH_HEADER);
		}
		if(!jwt.startsWith(UtilString.BEARER.value())) {
			throw new CustomException(TokenExceptionType.NOT_BEARER_TOKEN);
		}


		if (StringUtils.hasText(jwt) && jwt.startsWith(UtilString.BEARER.value())) {
			jwt = jwt.substring(7);
		}
		return jwt;
	}

	private String getTokenFromCookie(HttpServletRequest request) {
		String refreshToken = null;
		for(Cookie cookie : request.getCookies()) {
			if (cookie.getName().equals(UtilString.COOKIE_NAME.value())) {
				refreshToken = cookie.getValue();
				break;
			}
		}
		return refreshToken;
	}

	private void ignoreRequest(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{
		String requestUri = request.getRequestURI();
		log.info("request URI: {}", request.getRequestURI());
		for(String uri:permitList) {
			if (requestUri.equals("/")) {
				filterChain.doFilter(request, response);
			}
			if (requestUri.contains(uri)) {
				filterChain.doFilter(request, response);
			}
		}
	}
}
