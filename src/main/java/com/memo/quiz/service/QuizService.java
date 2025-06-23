package com.memo.quiz.service;

import static com.memo.common.enums.SortType.*;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.memo.category.entity.Category;
import com.memo.category.repository.CategoryRepository;
import com.memo.common.enums.SortType;
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

import io.lettuce.core.GeoArgs;
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

	@Transactional
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

	@Transactional
	public void deleteById(Long id, User user) {
		Quiz result = quizRepository.findByIdAndIsDeletedFalse(id);
		Optional.ofNullable(result).orElseThrow(() -> new CustomException(ExceptionType.NOT_FOUND_QUIZ, id));
		if (!result.getUser().getId().equals(user.getId())) {
			throw new CustomException(ExceptionType.UNAUTHORIZED);
		}
		result.delete();
	}

	public List<QuizResponseDto> findByCategoryId(Long categoryId, User user, SortType type) {
		if(user.getIsDeleted()) {
			throw new CustomException(ExceptionType.NOT_FOUND_USER);
		}
		List<Quiz> results = quizRepository.findByCategoryIdAndUserIdIsDeletedFalse(categoryId, user.getId());
		List<Quiz> deletedCategory = results.stream().filter((result) -> result.getCategory().getIsDeleted()).toList();
		if(!deletedCategory.isEmpty()) {
			throw new CustomException(ExceptionType.IS_DELETED_RESOURCE);
		}
		List<QuizResponseDto> response = Quiz.entityToDto(results);
		List<QuizResponseDto> sortedResponse =  sortByType(response, type);

		return sortedResponse;
	}

	private List<QuizResponseDto> sortByType(List<QuizResponseDto> list, SortType type) {
		List<QuizResponseDto> result = switch (type) {
			case CREATED_AT_ASC -> list.stream().sorted(Comparator.comparing(QuizResponseDto::getCreatedAt)).toList();
			case CREATED_AT_DESC -> list.stream().sorted(Comparator.comparing(QuizResponseDto::getCreatedAt).reversed()).toList();
			case EN_ASC -> list.stream().sorted(Comparator.comparing(QuizResponseDto::getQuestion)).toList();
			case KO_ASC -> list.stream()
				.sorted(Comparator.comparing(QuizResponseDto::getQuestion, Collator.getInstance(Locale.KOREA))).toList();
		};
		return result;
	}

	public List<QuizResponseDto> findByUser(User user, SortType type) {
		if(user.getIsDeleted()) {
			throw new CustomException(ExceptionType.NOT_FOUND_USER);
		}
		List<Quiz> results = quizRepository.findByUserIdAndIsDeletedFalse(user.getId());
		List<QuizResponseDto> response = Quiz.entityToDto(results);
		List<QuizResponseDto> sortedResponse =  sortByType(response, type);
		return sortedResponse;
	}

}
