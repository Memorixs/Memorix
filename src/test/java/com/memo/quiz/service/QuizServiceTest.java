package com.memo.quiz.service;

import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Nested;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.memo.category.entity.Category;
import com.memo.category.repository.CategoryRepository;
import com.memo.common.exception.CustomException;
import com.memo.common.exception.ExceptionType;
import com.memo.quiz.DTO.CreateQuizRequestDto;
import com.memo.quiz.DTO.ModifiedQuizRequestDto;
import com.memo.quiz.DTO.QuizResponseDto;
import com.memo.quiz.entity.Quiz;
import com.memo.quiz.entity.Status;
import com.memo.quiz.repository.QuizRepository;
import com.memo.user.entity.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

	@InjectMocks
	QuizService quizService;

	@Mock
	QuizRepository quizRepository;
	@Mock
	CategoryRepository categoryRepository;

	@Mock
	Quiz found;

	User user;

	ModifiedQuizRequestDto modifiedRequest;
	CreateQuizRequestDto quiz;
	Status status = Status.NONE;
	private final String question = "question";
	private final String answer = "answer";
	private final String ref = "ref";
	private final String categoryStr = "category";
	@BeforeEach
	public void init() {
		quiz = new CreateQuizRequestDto(question, answer, ref, status, categoryStr);
		modifiedRequest = new ModifiedQuizRequestDto("newTitle", "newContent", "ref","changeCategory", Status.KNOWN);
		user = new User();
		ReflectionTestUtils.setField(user, "id", 1L);
		// doReturn("category").when(category).getName();

	}

	@Nested
	@DisplayName("특정 카테고리에 종속된 자료 삭제하기")
	@Test
	void deleteByCategory() {

	}

	@Nested
	@DisplayName("자료 질문 중복으로 실패")
	@Test
	void saveFailed() {
		//given
		doReturn(Quiz.builder().question(quiz.getQuestion()).build()).when(quizRepository).findByQuestion(quiz.getQuestion()); //중복된 질문을 등록하려할 때 Quiz가 반환되어야한다.
		//when
		final CustomException result = assertThrows(CustomException.class, () -> quizService.save(user, quiz));
		//then
		assertThat(result.getException()).isEqualTo(ExceptionType.DUPLICATED_QUESTION);

	}

	@Nested
	@DisplayName("존재하지 않은 카테고리로 등록하여 실패")
	@Test
	void saveFailedByCategory() {
		//given
		doReturn(null).when(categoryRepository).findByUserIdAndNameAndIsDeletedFalse(any(Long.class), eq(quiz.getCategory()));
		//when
		final CustomException result = assertThrows(CustomException.class, () -> quizService.save(user, quiz));
		//then
		assertThat(result.getException()).isEqualTo(ExceptionType.NOT_FOUND_CATEGORY);
		verify(categoryRepository, times(1)).findByUserIdAndNameAndIsDeletedFalse(user.getId(), quiz.getCategory());
	}

	@Nested
	@DisplayName("자료 등록 성공")
	@Test
	void save() {
		//dto로 들어온 값이 entity로 잘 저장이 되었는지
		//given
		// doReturn(1L).when(user).getId();

		Category category = Category.createByUser(categoryStr, user);
		doReturn(null).when(quizRepository).findByQuestion(quiz.getQuestion());
		doReturn(category).when(categoryRepository).findByUserIdAndNameAndIsDeletedFalse(user.getId(), quiz.getCategory());
		doReturn(quiz(user)).when(quizRepository).save(any(Quiz.class));
		//not null
		//when
		final QuizResponseDto result = quizService.save(user, quiz);
		//then
		assertThat(result.getQuestion()).isEqualTo(quiz.getQuestion());
		assertThat(result.getAnswer()).isEqualTo(quiz.getAnswer());
		assertThat(result.getCategory()).isEqualTo(quiz.getCategory());
		assertThat(result.getReference()).isEqualTo(quiz.getReference());
		assertThat(result.getStatus()).isEqualTo(quiz.getStatus());
		assertFalse(result.isBookmarked());
		assertThat(result.getStatus()).isEqualTo(Status.NONE);
		assertThat(result.getUserId()).isEqualTo(user.getId());
		assertNotNull(result.getId());
		//verity
		verify(quizRepository, times(1)).findByQuestion(any(String.class));
		verify(quizRepository, times(1)).save(any(Quiz.class));
		verify(categoryRepository, times(1)).findByUserIdAndNameAndIsDeletedFalse(any(Long.class), eq(quiz.getCategory()));

	}
	private Quiz quiz(User user) {


		return Quiz.builder()
			.id(1L)
			.answer(answer)
			.question(question)
			.reference(ref)
			.category(Category.createByUser(categoryStr, user))
			.isBookmarked(false)
			.isDeleted(false)
			.status(status)
			.user(user)
			.build();
	}

	@Nested
	@DisplayName("존재하지 않는 자료 삭제 시도로 실패")
	@Test
	void notFoundDeleteFailed() {
		//given
		doReturn(null).when(quizRepository).findByIdAndIsDeletedFalse(anyLong());
		//when
		final CustomException result = assertThrows(CustomException.class, () -> quizService.deleteById(1L, user));
		//then
		assertThat(result.getException()).isEqualTo(ExceptionType.NOT_FOUND_QUIZ);
		//verify
		verify(quizRepository, times(1)).findByIdAndIsDeletedFalse(anyLong());
	}

	@Nested
	@DisplayName("권한이 없는 사용자가 삭제 시도로 인해 실패")
	@Test
	void noAuthDeleteFailed() {
		//given
		doReturn(quiz(user)).when(quizRepository).findByIdAndIsDeletedFalse(anyLong());

		User forbiddenUser = new User();
		ReflectionTestUtils.setField(forbiddenUser, "id", 2L);
		//when
		final CustomException result = assertThrows(CustomException.class, () -> quizService.deleteById(1L, forbiddenUser));
		//then
		assertThat(result.getException()).isEqualTo(ExceptionType.UNAUTHORIZED);
		verify(quizRepository, times(1)).findByIdAndIsDeletedFalse(anyLong());
	}

	@Nested
	@DisplayName("id로 자료 삭제 성공")
	@Test
	void deleteById() {
		//학습자료는 softdelete
		//given
		Quiz found= quiz(user);
		doReturn(found).when(quizRepository).findByIdAndIsDeletedFalse(found.getId());
		// deleteById(id)는 void 메서드이고, Mockito에서는 기본적으로 void 메서드는 아무 동작도 하지 않는 것처럼 동작합니다.
		//when
		quizService.deleteById(found.getId(), user);
		//then
		assertTrue(found.isDeleted());
		verify(quizRepository, times(1)).findByIdAndIsDeletedFalse(anyLong());

		// assertAll(
			// () -> verify(quizRepository.findById(any(Long.class)));
			// () -> assertTrue(found.isDeleted()),
		// )
	}

	@Nested
	@DisplayName("카테고리 id 별로 자료 조회")
	@Test
	void findAllByCategoryId() {
	}

	@Nested
	@DisplayName("미분류 자료를 특정 카테고리에 등록")
	@Test
	void updateCategoryById() {

	}
}