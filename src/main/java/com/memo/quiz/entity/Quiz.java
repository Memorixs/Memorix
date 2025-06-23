package com.memo.quiz.entity;

import java.time.LocalDateTime;

import com.memo.Resource;
import com.memo.category.entity.Category;
import com.memo.quiz.DTO.CreateQuizRequestDto;
import com.memo.quiz.DTO.QuizResponseDto;
import com.memo.user.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@AllArgsConstructor
@Builder
public class Quiz implements Resource {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String question;
	private String answer;
	private boolean isBookmarked;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private boolean isDeleted;
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;
	@ManyToOne(fetch = FetchType.LAZY)
	private Category category;
	private String reference;
	private Status status;

	public static Quiz of(CreateQuizRequestDto quiz, User user, Category category) {
		return Quiz.builder()
			.question(quiz.getQuestion())
			.answer(quiz.getAnswer())
			.isBookmarked(false)
			.status(Status.NONE)
			.isDeleted(false)
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.reference(quiz.getReference())
			.user(user)
			.category(category)
			.build();

	}

	public static QuizResponseDto entityToDto(Quiz entity) {
		return QuizResponseDto.builder()
			.question(entity.getQuestion())
			.answer(entity.getAnswer())
			.isBookmarked(entity.isBookmarked())
			.status(entity.getStatus())
			.reference(entity.getReference())
			.id(entity.getId())
			.category(entity.getCategory().getName())
			.userId(entity.getUser().getId())
			.build();
	}

	public void delete() {
		this.isDeleted = true;
	}
}
