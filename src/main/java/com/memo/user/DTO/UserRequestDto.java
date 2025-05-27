package com.memo.user.DTO;


public interface UserRequestDto {
	default String getUsername() {
		return "";
	}
	String getEmail();
	String getPassword();
}
