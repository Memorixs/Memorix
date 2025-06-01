package com.memo.user.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupFormRequestDto implements UserRequestDto{
	private String email;
	private String username;
	private String password;
}
