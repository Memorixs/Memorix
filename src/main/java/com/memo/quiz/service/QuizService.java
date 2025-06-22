package com.memo.quiz.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.memo.category.entity.Category;
import com.memo.category.repository.CategoryRepository;
import com.memo.common.exception.CustomException;
import com.memo.common.exception.ExceptionType;
import com.memo.quiz.DTO.CreateQuizRequestDto;
import com.memo.quiz.DTO.ModifiedQuizRequestDto;
import com.memo.quiz.DTO.QuizDto;
import com.memo.quiz.DTO.QuizResponseDto;
import com.memo.quiz.entity.Quiz;
import com.memo.quiz.entity.Status;
import com.memo.quiz.repository.QuizRepository;
import com.memo.user.entity.User;

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

	public QuizResponseDto save(User user, CreateQuizRequestDto quiz) {
		Quiz result = quizRepository.findByQuestion(quiz.getQuestion());
		if (result != null) {
			throw new CustomException(ExceptionType.DUPLICATED_QUESTION, quiz.getQuestion());
		}

		Category category = categoryRepository.findByUserIdAndNameAndIsDeletedFalse(user.getId(), quiz.getCategory());
		if (category == null) {
			throw new CustomException(ExceptionType.NOT_FOUND_CATEGORY, quiz.getCategory());
		}
		//user를 조회하지 않아도 되는구나!

		Quiz entity = Quiz.of(quiz, user, category);

		Quiz saved = quizRepository.save(entity);
		QuizResponseDto response = Quiz.entityToDto(saved);
		return response;
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

	public void deleteById(Long id) {

	}

	public List<QuizDto> findByCategoryId(Long categoryId) {
		return List.of();
	}
}
