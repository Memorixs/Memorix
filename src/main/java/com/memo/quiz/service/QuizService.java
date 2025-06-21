package com.memo.quiz.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.memo.category.entity.Category;
import com.memo.category.repository.CategoryRepository;
import com.memo.quiz.DTO.CreateQuizRequestDto;
import com.memo.quiz.DTO.ModifiedQuizRequestDto;
import com.memo.quiz.DTO.QuizDto;
import com.memo.quiz.entity.Quiz;
import com.memo.quiz.entity.Status;
import com.memo.quiz.repository.QuizRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuizService {
	private final QuizRepository quizRepository;
	private final CategoryRepository categoryRepository;

	public void deleteByCategory(Category category) {
		quizRepository.deleteByCategory(category);
	}

	public List<Long> saveAll(List<CreateQuizRequestDto> notes) {
		return List.of();
	}

	public Long save(CreateQuizRequestDto quiz) {
		return null;
	}

	public List<QuizDto> updateCategory(List<ModifiedQuizRequestDto> notes) {
		return List.of();
	}

	public QuizDto update(Long id, ModifiedQuizRequestDto notes) {
		return null;
	}

	public QuizDto updateStatus(Quiz quiz, Status status) {
		return null;
	}

	public void deleteAll(List<Quiz> quizzes) {

	}

	public List<QuizDto> findByCategoryId(Long categoryId) {
		return List.of();
	}
}
