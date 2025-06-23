package com.memo.quiz.service;

import static java.time.Month.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;

import java.text.Collator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Nested;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWarDeployment;
import org.springframework.test.util.ReflectionTestUtils;

import com.memo.category.dto.ListResponse;
import com.memo.category.entity.Category;
import com.memo.category.repository.CategoryRepository;
import com.memo.common.enums.SortType;
import com.memo.common.exception.CustomException;
import com.memo.common.exception.ExceptionType;
import com.memo.quiz.DTO.CreateQuizRequestDto;
import com.memo.quiz.DTO.ModifiedQuizRequestDto;
import com.memo.quiz.DTO.QuizByCategoryResponseDto;
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

	Quiz found;

	User user;


	Category category;
	Category c1,c2;
	Category deltedCategory;
	Quiz found1, found2, q1,q2, q3;
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
		ReflectionTestUtils.setField(user, "isDeleted", false);
		// doReturn("category").when(category).getName();

		category = new Category();
		ReflectionTestUtils.setField(category, "user", user);
		ReflectionTestUtils.setField(category, "id", 1L);
		ReflectionTestUtils.setField(category, "isDeleted", false);
		ReflectionTestUtils.setField(category, "name", categoryStr);
		// ReflectionTestUtils.setField(category, "quizzes", new ArrayList<>(List.of(found1, found2)));
		found1 = Quiz.builder()
			.category(category) //카테고리가 isDeleted = true이면 null 이어야함
			.user(user)
			.question("한글english")
			.createdAt(LocalDateTime.of(2025, APRIL, 20, 4, 23))
			.isDeleted(false)
			.build();


		found2 = Quiz.builder()
			.category(category) //카테고리가 isDeleted = true이면 null 이어야함
			.user(user)
			.question("english한글")
			.createdAt(LocalDateTime.of(2025, JUNE, 23, 4, 23))
			.isDeleted(false)
			.build();

		c1 = new Category();
		ReflectionTestUtils.setField(c1, "name", "다른 카테고리");
		ReflectionTestUtils.setField(c1, "user", user);


		c2 = new Category();
		ReflectionTestUtils.setField(c1, "name", "또 다른 카테고리");
		ReflectionTestUtils.setField(c1, "user", user);

		q1 = Quiz.builder()
			.category(c1) //카테고리가 isDeleted = true이면 null 이어야함
			.user(user)
			.question("english한글")
			.createdAt(LocalDateTime.of(2025, JUNE, 23, 4, 23))
			.isDeleted(false)
			.build();

		q2 = Quiz.builder()
			.user(user)
			.category(c1)
			.question("한글english한글")
			.createdAt(LocalDateTime.of(2025, JUNE, 23, 4, 23))
			.isDeleted(false)
			.build();

		q3 = Quiz.builder()
			.user(user)
			.category(c2)
			.question("Eenglish한글")
			.createdAt(LocalDateTime.of(2025, JUNE, 23, 4, 23))
			.isDeleted(false)
			.build();
		category.getQuizzes().add(found1);
		category.getQuizzes().add(found2);
		c1.getQuizzes().add(q1);
		c1.getQuizzes().add(q2);
		c2.getQuizzes().add(q3);
	}

	@Nested
	@DisplayName("특정 카테고리에 종속된 자료 삭제하기")
	@Test
	void deleteByCategory() {

	}

	@Nested
	@DisplayName("자료 질문 중복으로 실패") //특정 카테고리 안에서 중복될 경우에만 실패
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
	@DisplayName("삭제된 카테고리로 조회되어서 실패") //카테고리가 삭제되면 학습 자료는 미분류로..
	@Test
	void findDeletedFailed() {
		//given

		Category deltedCategory = new Category();
		ReflectionTestUtils.setField(deltedCategory, "user", user);
		ReflectionTestUtils.setField(deltedCategory, "id", 1L);
		ReflectionTestUtils.setField(deltedCategory, "isDeleted", true);

		Quiz found = Quiz.builder()
			.category(deltedCategory)
			.build();
		SortType type = SortType.CREATED_AT_DESC;

		doReturn(List.of(found)).when(quizRepository).findByCategoryIdAndUserIdIsDeletedFalse(deltedCategory.getId(), user.getId());
		//when
		final CustomException result = assertThrows(CustomException.class, () -> quizService.findByCategoryId(deltedCategory.getId(), user, type));
		//then
		assertThat(result.getException()).isEqualTo(ExceptionType.IS_DELETED_RESOURCE);
		verify(quizRepository, times(1)).findByCategoryIdAndUserIdIsDeletedFalse(category.getId(), category.getUser().getId());
	}

	@Nested
	@DisplayName("카테고리별 조회에서 삭제된 회원이 조회한 경우 실패")
	@Test
	void findByCategoryFailed() {
		//given
		User deletedUser = new User();
		ReflectionTestUtils.setField(deletedUser, "isDeleted", true);
		ReflectionTestUtils.setField(deletedUser, "id", 1L);

		//when
		final CustomException result = assertThrows(CustomException.class, () -> quizService.findByCategoryId(category.getId(), deletedUser, SortType.KO_ASC));
		//then
		assertThat(result.getException()).isEqualTo(ExceptionType.NOT_FOUND_USER);
	}


	@Nested
	@DisplayName("카테고리 id 별로 정렬(최신순) 자료 조회 성공")
	@Test
	void findAllByCategoryIdCreatedAtDesc() {
		//given
		SortType type = SortType.CREATED_AT_DESC;
		doReturn(new ArrayList<>(List.of(found1, found2))).when(quizRepository).findByCategoryIdAndUserIdIsDeletedFalse(category.getId(), user.getId());
		//when
		List<QuizResponseDto> results = quizService.findByCategoryId(category.getId(), user, type);
		//then
		//최신순으로 정렬되었는지 확인
		List<LocalDateTime> timestamps = results.stream()
			.map(QuizResponseDto::getCreatedAt)
			.collect(Collectors.toList());

		List<LocalDateTime> sorted = new ArrayList<>(timestamps);
		sorted.sort(Comparator.reverseOrder());

		assertTrue(results.stream().allMatch(quiz -> quiz.getCategory().equals(categoryStr)));
		assertTrue(results.stream().allMatch(quiz -> quiz.getUserId().equals(user.getId())));
		assertThat(timestamps).isEqualTo(sorted);
		verify(quizRepository, times(1)).findByCategoryIdAndUserIdIsDeletedFalse(category.getId(), user.getId());

	}

	@Nested
	@DisplayName("카테고리 id 별로 정렬(오래된순) 자료 조회 성공")
	@Test
	void findAllByCategoryIdCreatedAtAsc() {
		//given
		SortType type = SortType.CREATED_AT_ASC;
		doReturn(new ArrayList<>(List.of(found1, found2))).when(quizRepository).findByCategoryIdAndUserIdIsDeletedFalse(category.getId(), user.getId());
		//when
		List<QuizResponseDto> results = quizService.findByCategoryId(category.getId(), user, type);
		//then
		//최신순으로 정렬되었는지 확인
		List<LocalDateTime> timestamps = results.stream()
			.map(QuizResponseDto::getCreatedAt)
			.collect(Collectors.toList());

		List<LocalDateTime> sorted = new ArrayList<>(timestamps);
		sorted.sort(Comparator.naturalOrder());

		assertTrue(results.stream().allMatch(quiz -> quiz.getCategory().equals(categoryStr)));
		assertTrue(results.stream().allMatch(quiz -> quiz.getUserId().equals(user.getId())));
		assertThat(timestamps).isEqualTo(sorted);
		verify(quizRepository, times(1)).findByCategoryIdAndUserIdIsDeletedFalse(category.getId(), user.getId());

	}

	@Nested
	@DisplayName("카테고리 id 별로 정렬(가나다순) 자료 조회 성공")
	@Test
	void findAllByCategoryIdKo() {
		//given

		SortType type = SortType.KO_ASC;
		doReturn(new ArrayList<>(List.of(found1, found2))).when(quizRepository).findByCategoryIdAndUserIdIsDeletedFalse(category.getId(), user.getId());
		//when
		List<QuizResponseDto> results = quizService.findByCategoryId(category.getId(), user, type);
		//then
		//최신순으로 정렬되었는지 확인
		List<String> questions = results.stream()
			.map(QuizResponseDto::getQuestion)
			.collect(Collectors.toList());

		List<String> sorted = new ArrayList<>(questions);
		sorted.sort(Collator.getInstance(Locale.KOREA));

		assertTrue(results.stream().allMatch(quiz -> quiz.getCategory().equals(categoryStr)));
		assertTrue(results.stream().allMatch(quiz -> quiz.getUserId().equals(user.getId())));
		assertThat(questions).isEqualTo(sorted);
		verify(quizRepository, times(1)).findByCategoryIdAndUserIdIsDeletedFalse(category.getId(), user.getId());

	}

	@Nested
	@DisplayName("카테고리 id 별로 정렬(알파벳순) 자료 조회 성공")
	@Test
	void findAllByCategoryIdEn() {
		//given

		SortType type = SortType.EN_ASC;
		doReturn(new ArrayList<>(List.of(found1, found2))).when(quizRepository).findByCategoryIdAndUserIdIsDeletedFalse(category.getId(), user.getId());
		//when
		List<QuizResponseDto> results = quizService.findByCategoryId(category.getId(), user, type);
		//then
		//최신순으로 정렬되었는지 확인
		List<String> questions = results.stream()
			.map(QuizResponseDto::getQuestion)
			.collect(Collectors.toList());

		List<String> sorted = new ArrayList<>(questions);
		sorted.sort(Comparator.naturalOrder());

		assertTrue(results.stream().allMatch(quiz -> quiz.getCategory().equals(categoryStr)));
		assertTrue(results.stream().allMatch(quiz -> quiz.getUserId().equals(user.getId())));
		assertThat(questions).isEqualTo(sorted);
		verify(quizRepository, times(1)).findByCategoryIdAndUserIdIsDeletedFalse(category.getId(), user.getId());

	}



	@Nested
	@DisplayName("미분류 자료를 특정 카테고리에 등록")
	@Test
	void updateCategoryById() {

	}

	@Nested
	@DisplayName("삭제된 회원이 조회한 경우")
	@Test
	void findByUserIdFailed() {
		//given
		User deletedUser = new User();
		ReflectionTestUtils.setField(deletedUser, "isDeleted", true);
		ReflectionTestUtils.setField(deletedUser, "id", 1L);

		//when
		final CustomException result = assertThrows(CustomException.class, () -> quizService.findByUser(deletedUser, SortType.CREATED_AT_DESC));
		//then
		assertThat(result.getException()).isEqualTo(ExceptionType.NOT_FOUND_USER);
	}

	@Nested
	@DisplayName("유저가 등록한 모든 자료 조회 성공(최신순으로 정렬)")
	@Test
	void findByUser() {
		//given
		SortType type = SortType.CREATED_AT_DESC;
		// ReflectionTestUtils.setField(category, "quizzes", new ArrayList<>(List.of(found1, found2)));
		// ReflectionTestUtils.setField(c1, "quizzes", new ArrayList<>(List.of(q1, q2)));
		// ReflectionTestUtils.setField(c2, "quizzes", new ArrayList<>(List.of(q3)));
		// category.getQuizzes().add(found1);
		// category.getQuizzes().add(found2);
		// c1.getQuizzes().add(q1);
		// c1.getQuizzes().add(q2);
		// c2.getQuizzes().add(q3);

		doReturn(new ArrayList<>(List.of(category, c1, c2))).when(categoryRepository)
			.findByUserIdAndIsDeletedFalse(user.getId());
		//when
		ListResponse<QuizByCategoryResponseDto> results = quizService.findByUser(user, type);
		//then
		//최신순으로 정렬되었는지 확인
		List<LocalDateTime> timestamps = results.getResponse().get(0).getQuizzes().stream()
			.map(QuizResponseDto::getCreatedAt)
			.collect(Collectors.toList());

		List<LocalDateTime> sorted = new ArrayList<>(timestamps);
		sorted.sort(Comparator.reverseOrder());

		assertThat(timestamps).isEqualTo(sorted);
		verify(categoryRepository, times(1)).findByUserIdAndIsDeletedFalse(user.getId());
	}

	@Nested
	@DisplayName("카테고리 id 별로 정렬(오래된순) 자료 조회 성공")
	@Test
	void findAllByUserCreatedAtAsc() {
		//given
		SortType type = SortType.CREATED_AT_ASC;
		doReturn(new ArrayList<>(List.of(category, c1, c2))).when(categoryRepository)
			.findByUserIdAndIsDeletedFalse(user.getId());
		//when
		ListResponse<QuizByCategoryResponseDto> results = quizService.findByUser(user, type);
		//then
		//최신순으로 정렬되었는지 확인
		List<LocalDateTime> timestamps = results.getResponse().get(0).getQuizzes().stream()
			.map(QuizResponseDto::getCreatedAt)
			.collect(Collectors.toList());

		List<LocalDateTime> sorted = new ArrayList<>(timestamps);
		sorted.sort(Comparator.naturalOrder());

		assertThat(timestamps).isEqualTo(sorted);
		verify(categoryRepository, times(1)).findByUserIdAndIsDeletedFalse(user.getId());

	}

	@Nested
	@DisplayName("카테고리 id 별로 정렬(가나다순) 자료 조회 성공")
	@Test
	void findAllByUserKo() {
		//given

		SortType type = SortType.KO_ASC;
		doReturn(new ArrayList<>(List.of(category, c1, c2))).when(categoryRepository)
			.findByUserIdAndIsDeletedFalse(user.getId());
		//when
		ListResponse<QuizByCategoryResponseDto> results = quizService.findByUser(user, type);
		//then
		//최신순으로 정렬되었는지 확인
		List<String> questions = results.getResponse().get(0).getQuizzes().stream()
			.map(QuizResponseDto::getQuestion)
			.collect(Collectors.toList());

		List<String> sorted = new ArrayList<>(questions);
		sorted.sort(Collator.getInstance(Locale.KOREA));

		assertThat(questions).isEqualTo(sorted);
		verify(categoryRepository, times(1)).findByUserIdAndIsDeletedFalse(user.getId());

	}

	@Nested
	@DisplayName("카테고리 id 별로 정렬(알파벳순) 자료 조회 성공")
	@Test
	void findAllByUserEn() {
		//given
		SortType type = SortType.EN_ASC;
		doReturn(new ArrayList<>(List.of(category, c1, c2))).when(categoryRepository)
			.findByUserIdAndIsDeletedFalse(user.getId());
		//when
		ListResponse<QuizByCategoryResponseDto> results = quizService.findByUser(user, type);
		//then
		//최신순으로 정렬되었는지 확인
		List<String> questions = results.getResponse().get(0).getQuizzes().stream()
			.map(QuizResponseDto::getQuestion)
			.collect(Collectors.toList());

		List<String> sorted = new ArrayList<>(questions);
		sorted.sort(Comparator.naturalOrder());

		assertThat(questions).isEqualTo(sorted);
		verify(categoryRepository, times(1)).findByUserIdAndIsDeletedFalse(user.getId());

	}

	//모든 자료 조회시 카테고리별로 반환하는게 가독성에 좋음
	@Nested
	@DisplayName("유저가 등록한 자료 카테고리별로 묶어서 반환")
	@Test
	void findByUserClusteredByCategory() {
		//given
		SortType type = SortType.EN_ASC;

		Quiz q1 = Quiz.builder()
			.category(category)
			.question("question입니다.")
			.user(user)
			.isDeleted(false)
			.build();
		Quiz q2 = Quiz.builder()
			.category(category)
			.question("한글로 된 질문입니다.")
			.user(user)
			.isDeleted(false)
			.build();
		category.getQuizzes().add(q1);
		category.getQuizzes().add(q2);

		Category category1 = new Category();
		ReflectionTestUtils.setField(category1, "name", "다른 카테고리");
		ReflectionTestUtils.setField(category1, "user", user);
		Quiz q3 = Quiz.builder()
			.category(category1)
			.question("question입니다.")
			.user(user)
			.isDeleted(false)
			.build();
		Quiz q4 = Quiz.builder()
			.category(category1)
			.question("question입니다. 질문이요.")
			.user(user)
			.isDeleted(false)
			.build();
		category1.getQuizzes().add(q3);
		category1.getQuizzes().add(q4);
		Category category2 = new Category();
		ReflectionTestUtils.setField(category2, "name", "또 다른 카테고리");
		ReflectionTestUtils.setField(category2, "user", user);
		Quiz q5 = Quiz.builder()
			.category(category2)
			.question("question입니다.")
			.user(user)
			.isDeleted(false)
			.build();
		category2.getQuizzes().add(q5);

		//카테고리 리포지토리에서 유저가 등록한 카테고리 조회후 거기서 자료를 찾는게 더 빠를 듯
		doReturn(new ArrayList<>(List.of(category, category1, category2))).when(categoryRepository).findByUserIdAndIsDeletedFalse(user.getId());
		//when
		ListResponse<QuizByCategoryResponseDto> results = quizService.findByUser(user, type);
		//then
		List<String> questions = results.getResponse().get(0).getQuizzes().stream()
			.map(QuizResponseDto::getQuestion)
			.collect(Collectors.toList());

		List<String> sorted = new ArrayList<>(questions);
		sorted.sort(Comparator.naturalOrder());
		assertThat(questions).isEqualTo(sorted);
		verify(categoryRepository, times(1)).findByUserIdAndIsDeletedFalse(user.getId());
	}

}