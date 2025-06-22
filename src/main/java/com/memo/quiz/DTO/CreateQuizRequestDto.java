package com.memo.quiz.DTO;

import com.memo.category.entity.Category;
import com.memo.quiz.entity.Status;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CreateQuizRequestDto {
	private String question;
	private String answer;
	private String reference;
	private Status status;
	private String category;

}
