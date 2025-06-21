package com.memo.quiz.DTO;

import com.memo.quiz.entity.Status;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ModifiedQuizRequestDto extends QuizDto {
	private String category;
	public ModifiedQuizRequestDto(String question, String answer, String ref, String category, Status status) {
		super(question, answer, ref, status);
		this.category = category;
	}
}
