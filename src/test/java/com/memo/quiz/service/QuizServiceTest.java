package com.memo.quiz.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

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

import com.memo.quiz.DTO.CreateQuizRequestDto;
import com.memo.quiz.DTO.ModifiedQuizRequestDto;
import com.memo.quiz.entity.Quiz;
import com.memo.quiz.entity.Status;
import com.memo.quiz.repository.QuizRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

	@InjectMocks
	QuizService quizService;

	@Mock
	QuizRepository quizRepository;

	List<CreateQuizRequestDto> request = new ArrayList<>();
	ModifiedQuizRequestDto modifiedRequest;
	CreateQuizRequestDto quiz;
	Status status;
	@BeforeEach
	public void init() {
		quiz = new CreateQuizRequestDto("question", "answer", "ref1", Status.NONE);
		modifiedRequest = new ModifiedQuizRequestDto("newTitle", "newContent", "ref","changeCategory", Status.KNOWN);
		status = Status.BOOKMARK;
	}


	@Nested
	@DisplayName("미지정 카테고리 학습자료 등록")
	@Test
	void save() {
		//dto로 들어온 값이 entity로 잘 저장이 되었는지
		//given
		//not null
		CreateQuizRequestDto statusIsNull = new CreateQuizRequestDto("q", "a", "ref", null);

		Quiz saved = new Quiz();
		ReflectionTestUtils.setField(saved, "id", 1L); //리플렉션으로 객체 구조 알아와서 프록시 생성?하는 것 같음
		ReflectionTestUtils.setField(saved, "question", "question");
		ReflectionTestUtils.setField(saved, "answer", "answer");
		ReflectionTestUtils.setField(saved, "status", Status.NONE);
		given(quizRepository.save(any(Quiz.class))).willReturn(saved);
		//when
		Long response = quizService.save(quiz); //save 안에 repository saev가 호출될텐데 테스트에서는 목업 객체를 사용해서 호출한다. 그래서 given으로 리포지토리가 어떤 역할을 하는지 알려줘야한다.
		given(quizRepository.save(any(Quiz.class))).willReturn(saved);
		//then
		verify(quizRepository).save(any(Quiz.class)); //Service.save메서드가 repository의 save를 호출하는지 메서드를 실제로 호출했는지 확인하는것
		assertAll(
			() -> verify(quizRepository).save(any(Quiz.class)), //리포지토리 의 save가 호출되는지
			() -> assertEquals(quiz.getQuestion(), "question"), //리포지토리 호출 후 저장된 값이랑 요청한 값이랑 동일한지
			() -> assertEquals(quiz.getAnswer(), "answer"),
			() -> assertEquals(quiz.getStatus(), Status.NONE),

			//DTO값 테스트
			() -> assertThrows(IllegalArgumentException.class,
				() -> quizService.save(statusIsNull))

		);

	}

}