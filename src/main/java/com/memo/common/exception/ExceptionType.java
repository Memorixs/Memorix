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
	DUPLICATED_CATEGORY("중복되는 카테고리 이름입니다. name: ", HttpStatus.BAD_REQUEST),
	EXIST_EMAIL("중복되는 이메일입니다. email: ", HttpStatus.BAD_REQUEST),
	EXPIRED_LINK("만료된 링크입니다. 다시 요청해주세요.", HttpStatus.BAD_REQUEST),
	NOT_FOUND_EMAIL("해당 이메일을 가진 회원이 가입되어 있지않습니다. 다시 회원가입 해주세요. Email: ", HttpStatus.BAD_REQUEST),
	NOT_FOUND_USER("가입된 정보가 없습니다.", HttpStatus.BAD_REQUEST),
	NOT_VERIFIED_USER("검증되지 않은 사용자입니다. verified: ", HttpStatus.BAD_REQUEST),
	LOGOUT_USER("로그아웃된 회원입니다.", HttpStatus.BAD_REQUEST),
	DUPLICATED_QUESTION("이미 존재하는 자료입니다. question: ", HttpStatus.BAD_REQUEST),
	NOT_FOUND_QUIZ("존재하지 않는 퀴즈입니다. quizId: ", HttpStatus.BAD_REQUEST),
	UNAUTHORIZED("권한이 없는 잘못된 요청입니다.", HttpStatus.FORBIDDEN),
	IS_DELETED_RESOURCE("삭제된 자원에 대한 접근입니다.", HttpStatus.BAD_REQUEST);
	private String message;
	private HttpStatus status;
}
