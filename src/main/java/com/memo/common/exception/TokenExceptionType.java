package com.memo.common.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TokenExceptionType {
	NOT_FOUND_TOKEN("리프레시 토큰이 만료되었거나 존재하지 않아 토큰 갱신이 불가능합니다.", HttpStatus.BAD_REQUEST,4000),
	EXPIRED_TOKEN("토큰이 만료되었습니다.", HttpStatus.BAD_REQUEST,4001),
	TOKEN_EXCEPTION("토큰 관련 오류입니다. JWT 형식이 맞는지 확인해주세요.", HttpStatus.BAD_REQUEST, 4002),
	EMPTY_AUTH_HEADER("AUTHORIZATION 헤더 값이 비었습니다.", HttpStatus.BAD_REQUEST, 4003),
	NOT_BEARER_TOKEN("토큰 값은 \"Bearer\" 로 시작해야합니다.", HttpStatus.BAD_REQUEST, 4004);
	private String message;
	private HttpStatus status;
	private int code;
}
