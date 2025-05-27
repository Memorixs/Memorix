package com.memo.common.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.memo.user.entity.User;
import com.memo.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService { //사용자 인증
	//굳이사용해야하나? 이미 제공하는데 우리 서비스의 User 클래스로 변경해야지

	private final UserRepository userRepository;
	@Override
	public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
		//토큰으로 유저정보 가져와서 id 전달

		//인증과정 -> username으로 userw조회, 있으면 인증, 없으면 인증 실패
		User user = userRepository.findById(Long.parseLong(id))
			.orElseThrow(() -> new UsernameNotFoundException("not found pk : "+ id));

		return CustomUserDetails.of(user);
	}
	//구현하는 이유: 토큰 검증 후 다음 필터로 가기전에 다음 필터에서 UserDetailsService를 사용해서 유저 검증을 하는데 그게 이걸로 사용되도록 하기 위함

}
