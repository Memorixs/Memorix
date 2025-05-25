package com.memo.common.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.memo.user.entity.User;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CustomUserDetails implements UserDetails, OAuth2User { //인증, 인가에 필요한 정보

	private User user;

	@Override
	public Map<String, Object> getAttributes() {
		return Map.of();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return String.valueOf(user.getId());// id는 사용자 고유 식별자입니다. username과 같은 역할을 합니다.
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	public static CustomUserDetails of(User user) {
		return new CustomUserDetails(user);
	}

	//OAuth2User
	@Override
	public String getName() {
		return String.valueOf(user.getId());
	}

	public static UserDetails from(User user) {
		return new CustomUserDetails(user);
	}
}
