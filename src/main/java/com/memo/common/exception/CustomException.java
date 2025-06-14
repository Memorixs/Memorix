package com.memo.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException{
	private String message;
	private  HttpStatus status;
	private int code;

	public CustomException(ExceptionType exception, Object value) {
		this.message = exception.getMessage() + value.toString();
		this.status = exception.getStatus();
	}

	public CustomException(ExceptionType exceptionType) {
		this.message = exceptionType.getMessage();
		this.status = exceptionType.getStatus();
	}

	public CustomException(TokenExceptionType exceptionType) {
		this.message = exceptionType.getMessage();
		this.status = exceptionType.getStatus();
		this.code = exceptionType.getCode();
	}

}
