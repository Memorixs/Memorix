package com.memo.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.memo.login.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class OAuth2LoginSecurityConfig {

	private final OAuth2UserService  customOAuthService;
	private final  JwtProperties jwtProperties;
	// private final JwtFilter jwtFilter;
	private final AuthenticationSuccessHandler customOAuth2SuccessHandler;
	private final AuthenticationFailureHandler customOAuth2FailureHandler;
	private final UserDetailsService customUserDetailsService;
	private final RefreshTokenStore refreshTokenStore;
	private final UserRepository userRepository;
	private final TokenBlackListStore tokenBlackListStore;

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.csrf((csrf) -> csrf.disable()) //GET 이외의 요청 허용하기 위해

			.sessionManagement((session) -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers(new AntPathRequestMatcher("/login/**")).permitAll()
				.requestMatchers(new AntPathRequestMatcher("/*")).permitAll()
				.requestMatchers(new AntPathRequestMatcher("/login/oauth2/code/kakao")).permitAll()
				.requestMatchers(new AntPathRequestMatcher("/logout/oauth2/kakao")).permitAll() //리다이렉트 허용


				.anyRequest().authenticated())

			// .oauth2Login(oauth2 -> oauth2
			// 	// .userInfoEndpoint(userInfo -> userInfo
			// 	// 	.userService(customOAuthService)
			// 	// )
			// 	.loginProcessingUrl("/login/oauth2")
			// 	.successHandler(customOAuth2SuccessHandler)
			// 	.failureHandler(customOAuth2FailureHandler))
			// .logout(logout -> logout
			// 	.logoutUrl("/logout")
			// 	.logoutSuccessUrl("/")
			// 	.invalidateHttpSession(true)
			// 	.deleteCookies("refresh-token"))

				// .defaultSuccessUrl("/test")

			//필터 추가
			.addFilterBefore(new JwtFilter(jwtProperties, customUserDetailsService, refreshTokenStore, userRepository,  tokenBlackListStore),
				UsernamePasswordAuthenticationFilter.class); //UsernamePasswordAuthenticationFilter: ID와 PW를 사용하는 Form기반 유저 인증을 처리하는 Filter



		return http.build();
	}


}

