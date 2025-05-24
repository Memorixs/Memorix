package com.memo.common;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TokenProvider {

	private final SecretKey secretKey;

	TokenProvider(JwtProperties jwtProperties) {
		this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(jwtProperties.getSecretKey()));
	}
	public  String create(String role, String id, Date expired){
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
			.subject(id)//사용자id
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

	public String createAccessToken(String role, Long id) {
		Date expired = Date.from(Instant.now().plus(3, ChronoUnit.HOURS));
		return create(role, String.valueOf(id), expired);
	}
	public String createRefreshToken(String role, Long id) {
		Date expired = Date.from(Instant.now().plus(7, ChronoUnit.DAYS));
		return create(role, String.valueOf(id), expired);
	}
}
