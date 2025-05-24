package com.memo.login.oauth;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.memo.common.CustomUserDetails;
import com.memo.common.JwtFilter;
import com.memo.common.Role;
import com.memo.login.repository.UserRepository;
import com.memo.login.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuthService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	private final UserRepository userRepository;
	private final OAuthService googleOAuthService;
	private final OAuthService kakaoOAuthService;
	// private final JwtFilter jwtFilter;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
		String clientName = userRequest.getClientRegistration().getClientName(); //이걸로 어떤 플랫폼을 통해 로그인했는지 구분 가능
		log.info("clientName: {}", clientName);
		//회원 존재여부를 확인하기 위해 service 커스텀 필요
		OAuth2User oAuth2User = delegate.loadUser(userRequest);

		log.info("OauthUser: {}", oAuth2User.toString()); //제대로 가져왔는지 확인

		OAuthUser oAuthUser = switch(clientName) {
			case("Google") -> googleOAuthService.service(oAuth2User);
			case("kakao") -> kakaoOAuthService.service(oAuth2User);
			default -> throw new IllegalStateException("Unsupported client: " + clientName);
		};
		
		log.info(oAuthUser.toString());

		//회원가입 했는지 확인
		String providerId = oAuthUser.getId();

		//회원이 없으면 회원가입
		User user = userRepository.findByProviderId(providerId)
			.orElseGet(() -> {
				User newUser = User.from(oAuthUser);
				newUser.setRole(Role.USER);
				userRepository.save(newUser);
				return newUser;
			});

		log.info("User {}", user.toString());
		//이미 존재하는 회원이면 로그인 진행 -> 토큰 발급

		return CustomUserDetails.of(user);
	}

	public User login(User user) {
		return userRepository.findByProviderId(user.getProviderId()).orElseGet(() -> {
			User newUser = userRepository.save(user);
			log.info("User {}", user.toString());
			return newUser;
		});

		//이미 존재하는 회원이면 로그인 진행 -> 토큰 발급
	}
}
