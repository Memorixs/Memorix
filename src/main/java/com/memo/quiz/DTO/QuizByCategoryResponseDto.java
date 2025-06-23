package com.memo.quiz.DTO;

import java.util.ArrayList;
import java.util.List;

import com.memo.category.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class QuizByCategoryResponseDto implements Response {
	private int count;
	private String category;
	@Setter
	private List<QuizResponseDto> quizzes;

	public QuizByCategoryResponseDto(int count, String category) {
		this.count = count;
		this.category = category;
	}
}
