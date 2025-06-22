package com.memo.quiz.DTO;

import com.memo.quiz.entity.Status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuizResponseDto {
	private Long id;
	private String question;
	private String answer;
	private String reference;
	private Status status;
	private String category;
	private boolean isBookmarked;
	private Long userId;
}
