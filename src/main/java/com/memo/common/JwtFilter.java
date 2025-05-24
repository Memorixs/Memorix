package com.memo.common;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.memo.login.User;
import com.memo.login.repository.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

// @Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
	// private static List<String> tokenList = new LinkedList<>();
	//리프레시 토큰 저장할때가 로그인할때임
	private static final String AUTHORIZATION = "Authorization";
	private static final String HEADER_STRING = "Bearer";
	private final SecretKey secretKey;
	private final UserDetailsService customUserDetailsService;
	private final RefreshTokenStore refreshTokenStore;
	private final UserRepository userRepository;
	private final TokenBlackListStore tokenBlackListStore;
	private final String[] permitList = {"/logout/oauth2/kakao"};

	public JwtFilter(JwtProperties jwtProperties, UserDetailsService customUserDetailsService,
		RefreshTokenStore refreshTokenStore, UserRepository userRepository, TokenBlackListStore tokenBlackListStore) {

		this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(jwtProperties.getSecretKey()));
		this.customUserDetailsService = customUserDetailsService;
		this.refreshTokenStore = refreshTokenStore;
		this.userRepository = userRepository;
		this.tokenBlackListStore = tokenBlackListStore;
	}
	//공식문서에 Filter를 구현하기보다	 OncePerRequestFilter 를 확장하라고 되어 있다. -> 각 요청당 한번만 invoke된다. 그리고 dofilterInternal이 HttpServletRequest HttpServletResponse 제공
	//Filter는 그냥 ServletRequest 을 제공
	// https://docs.spring.io/spring-security/reference/servlet/architecture.html#_adding_a_custom_filter
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		//인증이 필요하지 않은 요청
		String requestUri = request.getRequestURI();
		for(String uri:permitList) {
			if (uri.equals(requestUri)) {
				filterChain.doFilter(request, response);
			}
		}
		//토큰 꺼내기
		String jwt = null;
		jwt = request.getHeader(AUTHORIZATION);
		log.info("jwt: {}", jwt);
		if (jwt == null || !jwt.startsWith(HEADER_STRING)) {
			filterChain.doFilter(request, response);
			return;
		}
		// String token = jwt.replace(HEADER_STRING, "");
		String token = resolveToken(jwt);
		//블랙리스트에 등록된 토큰인지 확인
		log.info("블랙리스트에 등록된 토큰인가? {}", tokenBlackListStore.contains(token));
		if(tokenBlackListStore.contains(token)) {
			throw new RuntimeException("로그아웃된 회원입니다.");
		}

		log.info("get AccessToken from header: {}", token);

		//해시알고리즘으로 signature 암호화
		String result = validate(token); //id로 사용자 가져와서 securitycontextholder에 적재?


		if (result.equals("EXPIRED")) {
			//만료된 토큰은 리프레시토큰을 활용해서 토큰 발급
			//요청마다 securityContextHolder는 clear된다 -
			String refreshToken = null;
			User user = null;
			for(Cookie cookie : request.getCookies()) {
				if (cookie.getName().equals("refresh-token")) {
					refreshToken = cookie.getValue();
					String refresh = validate(refreshToken);
					Long userId = Long.valueOf(refresh);
					if (refresh.equals("EXPIRED")) {
						//예외: 재로그인

					}
					else if(refreshTokenStore.containsKey(userId)) {
						//id가지고 토큰 생성

						//디비에서 유저 정보 조회

						user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
						String accessToken = create(user.getRole().name(), user.getId(), Date.from(Instant.now().plus(3, ChronoUnit.HOURS)));

						//엑세스토큰 헤더에 넣어주기
						response.setHeader("Authorization", accessToken);

					}
					// getRefreshToken(refreshToken);
				}
			}

			result = String.valueOf(user.getId());
			//디비에서 리프레시 토큰 찾기 -> 일단 인메모리로
			//linkedList사용한 이유: 리프레시 기간 1주일이라고 하면 1주일에 한번씩을 삽입삭제가 일어날것,
			// tokenList.stream().filter(each -> each.equals(refreshToken));

		}
		setAuthentication(result);

		//다음 필터
		filterChain.doFilter(request, response);
	}

	private void setAuthentication(String id) {
		//인증된 유저 정보를 가지고 인증된 토큰 생성
		UserDetails userDetails = customUserDetailsService.loadUserByUsername(id);
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, id, userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private String validate(String token) {
		//형식이 다름
		try {

			Jws<Claims> claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
			String subject = claims.getPayload().getSubject();
			return subject; //userId
			//OK, we can trust this JWT

		} catch (ExpiredJwtException e) {
			e.getStackTrace();
			return "EXPIRED";
			//don't trust the JWT!
		} catch (JwtException e) {
			e.getStackTrace();
			return "EXCEPTION";
		}
	}

	public String create(String role, Long id, Date expired){
		log.info("Create Token with {} and {}", role, id);

		MacAlgorithm alg = Jwts.SIG.HS256; //or HS384 or HS256
		// SecretKey key = Jwts.SIG.HS256.key().build(); //secretkey를 랜덤으로 하면 서버다 재시작될때마다 key값이 변경되므로 고정된 값으로 해주어야하낟.
		// String key = alg.key().build();
		//HMAC (HS256 등) -> 비밀키를 이용해 헤더에서 정의한 알고리즘으로 해싱

		return Jwts.builder()                     // (1)

			.header()                                   // (2) optional
			.keyId("aKeyId")
			.add("alg", "HS256")
			.add("type", "JWT")

			.and()
			// .claim() //custom claim

			//claim
			/*
			* iss: 토큰 발급자 (issuer)
				sub: 토큰 제목 (subject)
				aud: 토큰 대상자 (audience)
				exp: 토큰의 만료시간 (expiraton), 시간은 NumericDate 형식으로 되어있어야 하며 (예: 1480849147370) 언제나 현재 시간보다 이후로 설정되어있어야합니다.
				nbf: Not Before 를 의미하며, 토큰의 활성 날짜와 비슷한 개념입니다. 여기에도 NumericDate 형식으로 날짜를 지정하며, 이 날짜가 지나기 전까지는 토큰이 처리되지 않습니다.
				iat: 토큰이 발급된 시간 (issued at), 이 값을 사용하여 토큰의 age 가 얼마나 되었는지 판단 할 수 있습니다.
				jti: JWT의 고유 식별자로서, 주로 중복적인 처리를 방지하기 위하여 사용됩니다. 일회용 토큰에 사용하면 유용합니다.
* */
			// .issuer("me")
			.subject(String.valueOf(id))//사용자id
			// .audience().add("you").and()
			.expiration(expired) //a java.util.Date
			// .notBefore(notBefore) //a java.util.Date
			.issuedAt(new Date()) // for example, now
			.id(UUID.randomUUID().toString()) //just an example id
			.claim("role", role)

			.signWith(secretKey, alg)                       // (4) if signing, or
			//.encryptWith(key, keyAlg, encryptionAlg)  //     if encrypting

			.compact();                                 // (5)
	}

	public static String resolveToken(String token) {
		if (StringUtils.hasText(token) && token.startsWith(HEADER_STRING)) {
			return token.substring(7);
		}
		return null;
	}

}
