package com.memo.common.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ExceptionType {
	EXIST_CATEGORY("이미 존재하는 카테고리입니다. name: ", HttpStatus.BAD_REQUEST);
	private String message;
	private HttpStatus status;
}
