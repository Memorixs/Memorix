package com.memo.user.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequestDto implements UserRequestDto{
	private String email;
	private String password;
}
