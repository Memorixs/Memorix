package com.memo.user.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupFormRequestDto {
	private String email;
	private String username;
	private String password;
}
