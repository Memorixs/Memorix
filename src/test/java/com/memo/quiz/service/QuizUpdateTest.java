package com.memo.quiz.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.memo.category.entity.Category;
import com.memo.category.repository.CategoryRepository;
import com.memo.common.exception.CustomException;
import com.memo.quiz.DTO.CreateQuizRequestDto;
import com.memo.quiz.DTO.ModifiedQuizRequestDto;
import com.memo.quiz.DTO.QuizDto;
import com.memo.quiz.entity.Quiz;
import com.memo.quiz.entity.Status;
import com.memo.quiz.repository.QuizRepository;

@Nested
@ExtendWith(MockitoExtension.class)
public class QuizUpdateTest {


	@InjectMocks
	QuizService quizService;

	@Mock
	QuizRepository quizRepository;

	@Mock
	Category category;

	@Mock
	CategoryRepository categoryRepository;

	@Mock
	Quiz found;
	@BeforeEach
	public void init() {

		// category = new Category();
		ReflectionTestUtils.setField(category, "name", "category");

		// found = new Quiz();
		ReflectionTestUtils.setField(found, "id", 1L); //리플렉션으로 객체 구조 알아와서 프록시 생성?하는 것 같음
		ReflectionTestUtils.setField(found, "question", "question");
		ReflectionTestUtils.setField(found, "answer", "answer");
		ReflectionTestUtils.setField(found, "status", Status.NONE);
		ReflectionTestUtils.setField(found, "reference", "ref");
		ReflectionTestUtils.setField(found, "category", category);

		given(quizRepository.findById(any(Long.class))).willReturn(Optional.of(found));

		//0. 모든 상태 변경
		//1. 질문 업데이트 -> 모든 칼럼을 업데이트하는데에 소요시간이 오래걸리나? 레코드를 업데이트하는것 어쨋든 쿼리는 나갈것 patch로 하면 api 하나로 해결 가능함~..
		//2. 답 업데이트
		//3. 참고자료 업데이트
		//4. 카테고리 업데이트(null, 이미 있는값)
		//5. 상태 업데이트(null, 다른상대값, 없는 상태값 -> 컨트롤러에서 역직렬화에서 막힐것, 컨트롤러 테스트에서 작성)

		//dto 값이 black, empty 일 경우 컨트로럴 단에서 거를 것 서비스 메서드에서 테스트할 필요없다. Null이면 프론트에서 아예 값을 넘기지 않은 것이므로 상관없음 사용자가 수정하지 않앗음으로 의미하므로

	}

	@Test
	@DisplayName("수정하려는 값 null일 경우")
	void nullUpdate() {
		//given
		ModifiedQuizRequestDto nullUpdate = new ModifiedQuizRequestDto(null, null, null, null, null); //오류

		//when
		QuizDto nullResponse = quizService.update(1L, nullUpdate);
		//then
		//변경사항이 없어야함.
		assertAll(
			() -> verify(quizRepository.findById(any(Long.class))),
			() -> assertEquals(nullResponse.getAnswer(), found.getAnswer()),
			() -> assertEquals(nullResponse.getCategory(), found.getCategory().getName()),
			() -> assertEquals(nullResponse.getQuestion(), found.getQuestion()),
			() -> assertEquals(nullResponse.getReference(), found.getReference()),
			() -> assertEquals(nullResponse.getStatus(), found.getStatus())
		);
	}

	@Test
	@DisplayName("질문만 수정")
	void 질문만_수정() {
		//when
		ModifiedQuizRequestDto questionUpdate = new ModifiedQuizRequestDto("modified", null, null, null, null);
		//when
		QuizDto questionResponse = quizService.update(1L, questionUpdate);
		//then
		assertAll(
			() -> verify(quizRepository.findById(any(Long.class))),
			() -> assertEquals(questionResponse.getAnswer(), found.getAnswer()),
			() -> assertEquals(questionResponse.getCategory(), found.getCategory().getName()),
			() -> assertEquals(questionResponse.getQuestion(), "modified"),
			() -> assertEquals(questionResponse.getReference(), found.getReference()),
			() -> assertEquals(questionResponse.getStatus(), found.getStatus())
		);

	}

	@Test
	@DisplayName("답만 수정")
	void answerUpdate() {
		//given
		ModifiedQuizRequestDto answerUpdate = new ModifiedQuizRequestDto(null, "modified", null, null, null);

		//when
		QuizDto answerResponse = quizService.update(1L, answerUpdate);
		//then
		assertAll(
			() -> verify(quizRepository.findById(any(Long.class))),
			() -> assertEquals(answerResponse.getAnswer(), "modified"),
			() -> assertEquals(answerResponse.getCategory(), found.getCategory().getName()),
			() -> assertEquals(answerResponse.getQuestion(), found.getQuestion()),
			() -> assertEquals(answerResponse.getReference(), found.getReference()),
			() -> assertEquals(answerResponse.getStatus(), found.getStatus())
		);
	}

	@Test
	@DisplayName("참고자료만 수정")
	void refUpdate() {
		//given
		ModifiedQuizRequestDto refUpdate = new ModifiedQuizRequestDto(null, null, "modified", null, null);

		//when
		QuizDto refResponse = quizService.update(1L, refUpdate);
		//then
		assertAll(
			() -> verify(quizRepository.findById(any(Long.class))),
			() -> assertEquals(refResponse.getAnswer(), found.getAnswer()),
			() -> assertEquals(refResponse.getCategory(), found.getCategory().getName()),
			() -> assertEquals(refResponse.getQuestion(), found.getQuestion()),
			() -> assertEquals(refResponse.getReference(), "modified"),
			() -> assertEquals(refResponse.getStatus(), found.getStatus())
		);
	}

	@Test
	@DisplayName("카테고리 값만 수정")
	void updateCategory() {
		//given
		ModifiedQuizRequestDto categoryUpdate = new ModifiedQuizRequestDto(null, null, null, "modified", null);


		//when
		QuizDto categoryResponse = quizService.update(1L, categoryUpdate);
		//then
		assertAll(
			() -> verify(quizRepository.findById(any(Long.class))),
			() -> verify(categoryRepository.findByName(categoryUpdate.getCategory())),
			() -> assertEquals(categoryResponse.getAnswer(), found.getAnswer()),
			() -> assertEquals(categoryResponse.getCategory(), "modified"),
			() -> assertEquals(categoryResponse.getQuestion(), found.getQuestion()),
			() -> assertEquals(categoryResponse.getReference(), found.getReference()),
			() -> assertEquals(categoryResponse.getStatus(), found.getStatus())
		);
	}

	@Test
	@DisplayName("없는 카테고리 값으로 업데이트 시도할 경우")
	void notFoundCategory() {
		//given
		ModifiedQuizRequestDto notFoundCategoryUpdate = new ModifiedQuizRequestDto(null, null, null, "notFountCategory", null);
		given(categoryRepository.findByName(any(String.class))).willReturn(null);
		//when
		QuizDto notFoundCategoryResponse = quizService.update(1L, notFoundCategoryUpdate);
		//then
		assertAll(
			() -> verify(quizRepository.findById(any(Long.class))),
			() -> verify(categoryRepository.findByName(notFoundCategoryResponse.getCategory())),
			() -> assertThrows(CustomException.class, () -> quizService.update(1L, notFoundCategoryUpdate))
		);
	}

	@Test
	@DisplayName("상태 값만 수정")
	void statusUpdate() {
		//given
		ModifiedQuizRequestDto statusUpdate = new ModifiedQuizRequestDto(null, null, null, null, Status.BOOKMARK);
		//when
		QuizDto statusResponse = quizService.update(1L, statusUpdate);
		//then
		assertAll(
			() -> verify(quizRepository.findById(any(Long.class))),
			() -> assertEquals(statusResponse.getAnswer(), found.getAnswer()),
			() -> assertEquals(statusResponse.getCategory(), found.getCategory().getName()),
			() -> assertEquals(statusResponse.getQuestion(), found.getQuestion()),
			() -> assertEquals(statusResponse.getReference(), found.getReference()),
			() -> assertEquals(statusResponse.getStatus(), Status.BOOKMARK)
		);
	}

}
