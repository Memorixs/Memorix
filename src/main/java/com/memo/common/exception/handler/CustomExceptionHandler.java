package com.memo.common.exception.handler;

import org.apache.logging.log4j.util.InternalException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.memo.common.exception.CustomException;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class CustomExceptionHandler {

	//예상 흐름:
	@ExceptionHandler(CustomException.class)
	public ResponseEntity<String> handler(CustomException exception) {
		log.info(exception.getMessage(), exception);
		return ResponseEntity.status(exception.getStatus()).body(exception.getMessage());
	}

	@ExceptionHandler(InternalException.class)
	public ResponseEntity<String> handler(InternalException exception) {
		log.info(exception.getMessage(), exception);
		return ResponseEntity.internalServerError().body(exception.getMessage());
	}


}
