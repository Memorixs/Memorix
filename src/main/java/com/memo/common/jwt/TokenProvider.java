package com.memo.common.jwt;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.memo.common.exception.CustomException;
import com.memo.common.exception.ExceptionType;
import com.memo.common.exception.TokenExceptionType;
import com.memo.common.util.UtilString;
import com.memo.common.properties.JwtProperties;
import com.memo.storage.TokenRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TokenProvider {

	private final SecretKey secretKey;
	private final UserDetailsService customUserDetailsService;
	private final TokenRepository tokenRepository;

	TokenProvider(JwtProperties jwtProperties, UserDetailsService customUserDetailsService, TokenRepository tokenRepository) {
		this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(jwtProperties.getSecretKey()));
		this.customUserDetailsService = customUserDetailsService;
		this.tokenRepository = tokenRepository;
	}
	public  String create(String role, String id, Date expired){
		log.info("Create Token with {} and {}", role, id);
		MacAlgorithm alg = Jwts.SIG.HS256; //or HS384 or HS256

		return Jwts.builder()                     // (1)

			.header()                                   // (2) optional
			.keyId("aKeyId")
			.add("alg", "HS256")
			.add("type", "JWT")

			.and()

			.subject(id)//사용자id
			.expiration(expired) //a java.util.Date
			.issuedAt(new Date()) // for example, now
			.id(UUID.randomUUID().toString()) //just an example id
			.claim("role", role)

			.signWith(secretKey, alg)                       // (4) if signing, or

			.compact();                                 // (5)
	}

	public String createEmailConfirmToken(String email) {
		Date expired = Date.from(Instant.now().plus(10, ChronoUnit.MINUTES));
		return create(null, email, expired);
	}
	public String createAccessToken(String role, Long id) {
		Date expired = Date.from(Instant.now().plus(3, ChronoUnit.HOURS));
		return create(role, String.valueOf(id), expired);
	}
	public String createRefreshToken(String role, Long id) {
		Date expired = Date.from(Instant.now().plus(7, ChronoUnit.DAYS));
		return create(role, String.valueOf(id), expired);
	}

	public String renewalAccessToken(String refreshToken) {
		String id = getClaims(refreshToken).getSubject();
		String token = findRefreshToken(id);
		String role = (String)getClaims(token).get("role"); //role을 가지고 오기위해 디비에 접근하기보다 리프레시 토큰안에 있는 정보 활용
		String accessToken = create(role, id,
			Date.from(Instant.now().plus(3, ChronoUnit.HOURS)));

		return accessToken;
	}

	public String findRefreshToken(String id) {
		return Optional.ofNullable(tokenRepository.findByKey("refresh;id" + id))
			.orElseThrow(() -> new CustomException(TokenExceptionType.NOT_FOUND_TOKEN));
	}

	public String validate(String jwt) {
		try {

			Jws<Claims> claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(jwt);
			String subject = claims.getPayload().getSubject();
			return subject; //userId
			//OK, we can trust this JWT
		} catch (ExpiredJwtException e) {
			throw new CustomException(TokenExceptionType.EXPIRED_TOKEN);
			//don't trust the JWT!
		} catch (JwtException e) {
			throw new CustomException(TokenExceptionType.TOKEN_EXCEPTION);
		}
	}

	public static String resolveToken(String token) {
		if (StringUtils.hasText(token) && token.startsWith(UtilString.BEARER.value())) {
			return token.substring(7);
		}
		return null;
	}

	public Claims getClaims(String token) {
		Jws<Claims> claims = null;
		try {

			claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
			return claims.getPayload(); //userId
			//OK, we can trust this JWT
		} catch (ExpiredJwtException e) {
			e.getStackTrace();
			return e.getClaims();
			//don't trust the JWT!
		}catch (JwtException e) {
			e.getStackTrace();
			throw new RuntimeException("토큰 예외가 발생했습니다.");
		}
	}

	public void setAuthentication(String token) {
		//인증된 유저 정보를 가지고 인증된 토큰 생성
		String id = getClaims(token).getSubject();
		UserDetails userDetails = customUserDetailsService.loadUserByUsername(id); //이미 여기서 security Context Holder에 인증된 유저가 들어가 있음?
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, id, userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	public boolean isBlackListUser(String token ,String id) {
		if(tokenRepository.findByKey("blackList;id" + id) != null) {
			if (tokenRepository.findByKey("blackList;id" + id).equals(token)) {
				throw new CustomException(ExceptionType.LOGOUT_USER);
			}
		}
		return false ;
	}
}

