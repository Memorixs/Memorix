package com.memo.quiz.DTO;

import com.memo.quiz.entity.Status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
abstract public class QuizDto {
	private String question;
	private String answer;
	private String reference;
	private Status status;
}
