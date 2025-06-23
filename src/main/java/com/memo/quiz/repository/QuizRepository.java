package com.memo.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.memo.category.entity.Category;
import com.memo.quiz.entity.Quiz;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

	@Modifying
	@Query("update Quiz q set q.isDeleted = true where q.category = :category")
	void deleteByCategory(Category category);

	Quiz findByQuestion(String question);

	Quiz findByIdAndIsDeletedFalse(Long id);
}
