package com.memo.quiz.DTO;

import com.memo.quiz.entity.Status;

public class CreateQuizRequestDto extends QuizDto {

	public CreateQuizRequestDto(String question, String answer, String reference, Status status) {
		super(question, answer, reference, status);
	}
}
