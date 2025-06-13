package com.memo.note.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.memo.category.entity.Category;
import com.memo.note.entity.Note;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

	@Modifying
	@Query("update Note n set n.isDeleted = true where n.category = :category")
	void deleteByCategory(Category category);
}
