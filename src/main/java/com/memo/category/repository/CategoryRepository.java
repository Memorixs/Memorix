package com.memo.category.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.memo.category.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
	List<Category> findByUserId(Long id);

	Category findByName(String name);

	Optional<Category> findByIdAndIsDeletedIsFalse(Long id);

	Category findByUserIdAndNameAndIsDeletedFalse(Long userId, String name);
}

