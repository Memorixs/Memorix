package com.memo.common.config;

import static org.springframework.security.config.Customizer.*;

import java.util.TimeZone;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.client.RestTemplate;

import com.memo.common.jwt.JwtFilter;
import com.memo.common.jwt.TokenProvider;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class OAuth2LoginSecurityConfig {
	private final TokenProvider tokenProvider;
	private final CorsConfig corsConfig;

	@PostConstruct
	public void started() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.csrf((csrf) -> csrf.disable()) //GET 이외의 요청 허용하기 위해
			.cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
			.sessionManagement((session) -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers(new AntPathRequestMatcher("/login/**")).permitAll()
				.requestMatchers(new AntPathRequestMatcher("/**")).permitAll() //메인 페이지 접근 허용
				.requestMatchers(new AntPathRequestMatcher("/api/login/**")).permitAll() //메인 페이지 접근 허용
				.requestMatchers(new AntPathRequestMatcher("/login/oauth2/code/kakao")).permitAll() //로그인 리다이렉트 허용
				.requestMatchers(new AntPathRequestMatcher("/logout/oauth2/kakao")).permitAll() //로그아웃 리다이렉트 허용
				.requestMatchers(new AntPathRequestMatcher("/api/signup")).permitAll() //회원가입
				.requestMatchers(new AntPathRequestMatcher("/api/auth/confirm")).permitAll() //이메일 확인 리다이렉트
				.requestMatchers(new AntPathRequestMatcher("/swagger-ui")).permitAll()
				.requestMatchers(new AntPathRequestMatcher("/v3/api-docs")).permitAll()

				.anyRequest().authenticated())

			.formLogin(withDefaults())
			// .successDefaultUrl()
			//필터 추가
			.addFilterBefore(new JwtFilter(tokenProvider),
				UsernamePasswordAuthenticationFilter.class); //UsernamePasswordAuthenticationFilter: ID와 PW를 사용하는 Form기반 유저 인증을 처리하는 Filter

		return http.build();
	}
}

