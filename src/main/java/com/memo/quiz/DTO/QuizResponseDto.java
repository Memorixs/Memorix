package com.memo.quiz.DTO;

import com.memo.quiz.entity.Status;

import lombok.Getter;

@Getter
public class QuizResponseDto extends QuizDto {
	private boolean isBookmarked;
	private boolean know;

	public QuizResponseDto(String title, String content, String ref, Status status, boolean isBookmarked, boolean know) {
		super(title, content, ref, status);
		this.isBookmarked = isBookmarked;
		this.know = know;
	}
}
