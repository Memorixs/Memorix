package com.memo.common.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ExceptionType {
	EXIST_CATEGORY("이미 존재하는 카테고리입니다. name: ", HttpStatus.BAD_REQUEST),
	FORBIDDEN("수정할 권한이 없습니다. userId: ", HttpStatus.FORBIDDEN),
	NOT_FOUND_CATEGORY("존재하지 않는 카테고리입니다. id: ", HttpStatus.NOT_FOUND),
	DUPLICATED_CATEGORY("중복되는 카테고리 이름입니다. name: ", HttpStatus.BAD_REQUEST);
	private String message;
	private HttpStatus status;
}
