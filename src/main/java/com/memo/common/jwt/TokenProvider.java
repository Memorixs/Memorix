package com.memo.common.jwt;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.memo.common.util.UtilString;
import com.memo.common.properties.JwtProperties;

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

	TokenProvider(JwtProperties jwtProperties, UserDetailsService customUserDetailsService) {
		this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(jwtProperties.getSecretKey()));
		this.customUserDetailsService = customUserDetailsService;
	}
	public  String create(String role, Long id, Date expired){
		log.info("Create Token with {} and {}", role, id);
		MacAlgorithm alg = Jwts.SIG.HS256; //or HS384 or HS256

		return Jwts.builder()                     // (1)

			.header()                                   // (2) optional
			.keyId("aKeyId")
			.add("alg", "HS256")
			.add("type", "JWT")

			.and()

			.subject(String.valueOf(id))//사용자id
			.expiration(expired) //a java.util.Date
			.issuedAt(new Date()) // for example, now
			.id(UUID.randomUUID().toString()) //just an example id
			.claim("role", role)

			.signWith(secretKey, alg)                       // (4) if signing, or

			.compact();                                 // (5)
	}

	public String createAccessToken(String role, Long id) {
		Date expired = Date.from(Instant.now().plus(3, ChronoUnit.HOURS));
		return create(role, id, expired);
	}
	public String createRefreshToken(String role, Long id) {
		Date expired = Date.from(Instant.now().plus(7, ChronoUnit.DAYS));
		return create(role, id, expired);
	}

	public String validate(String jwt) {
		try {

			Jws<Claims> claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(jwt);
			String subject = claims.getPayload().getSubject();
			return subject; //userId
			//OK, we can trust this JWT

		} catch (ExpiredJwtException e) {
			e.getStackTrace();
			return UtilString.EXPIRED.value();
			//don't trust the JWT!
		} catch (JwtException e) {
			e.getStackTrace();
			return UtilString.EXCEPTION.value();
		}
	}

	public static String resolveToken(String token) {
		if (StringUtils.hasText(token) && token.startsWith(UtilString.BEARER.value())) {
			return token.substring(7);
		}
		return null;
	}

	public Claims getClaims(String token) {
		Jws<Claims> claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
		return claims.getPayload();
	}

	public void setAuthentication(String id) {
		//인증된 유저 정보를 가지고 인증된 토큰 생성
		UserDetails userDetails = customUserDetailsService.loadUserByUsername(id);
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, id, userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}

