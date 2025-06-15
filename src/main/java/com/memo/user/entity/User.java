package com.memo.user.entity;

import java.time.LocalDateTime;

import com.memo.user.DTO.SignupFormRequestDto;
import com.memo.user.DTO.UserRequestDto;
import com.memo.user.oauth.OAuthUser;
import com.memo.user.oauth.kakao.KakaoInfo;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id; //
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@Entity
@NoArgsConstructor
public class User {
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private Long id;

	private String username;
	private String email;
	private String password;
	private String imgUrl;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private Boolean isDeleted;
	private Boolean isBlacklist; //primitive type 대신 객체를 사용하는 이유
	private Boolean status;
	private Integer warning_count;
	private String providerId;
	@Setter
	private Role role; // guest는 필요없어보임 -> 그럼 이넘을 사용할 이유가 없음

	@Enumerated(EnumType.STRING)
	private LoginType loginType;

	@Setter
	private Boolean isVerified;

	@PrePersist
	public void perPersist() {
		if(isBlacklist == null) {
			this.isBlacklist = false;
		}
		if(status == null) {
			this.status = true;
		}
		if (warning_count == null) {
			this.warning_count = 0;
		}
		if (isDeleted == null) {
			this.isDeleted = false;
		}
		if (createdAt == null) {
			this.createdAt = LocalDateTime.now();
		}
		if (updatedAt == null) {
			this.updatedAt = LocalDateTime.now();
		}
		if (isVerified == null){
			this.isVerified = false;
		}
	}
	private User(String email, String username, String password, String providerId, String imgUrl, LoginType loginType, boolean isVerified, Role role) {
		this.email = email;
		this.username = username;
		this.password = password;
		this.providerId = providerId;
		this.imgUrl = imgUrl;
		this.loginType = loginType;
		this.isVerified = isVerified;
		this.role = role;
	}
	public static User from(OAuthUser oAuthUser) {
		return new User(oAuthUser.getEmail(), oAuthUser.getUsername(), null, oAuthUser.getId(), oAuthUser.getProfileImg(), oAuthUser.getLoginType(), true, Role.USER);
	}

	public static User from(KakaoInfo oAuthUser) {
		return new User(oAuthUser.getEmail(), oAuthUser.getNickname(), null, oAuthUser.getId(), null, LoginType.KAKAO, true, Role.USER);
	}

	public static User of(UserRequestDto requestDto, String password) {

		return new User(requestDto.getEmail(), requestDto.getUsername(), password, null, null,
			LoginType.NATIVE, true, Role.USER);
	}



}
