package com.memo.category.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.memo.category.dto.CategoryResponse;
import com.memo.category.dto.ListResponse;
import com.memo.category.entity.Category;
import com.memo.category.repository.CategoryRepository;
import com.memo.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {
	private final CategoryRepository categoryRepository;

	public ListResponse<CategoryResponse> findByUser(User user) {
		List<Category> entities = categoryRepository.findByUserId(user.getId());
		List<CategoryResponse> response = Category.entityToDto(entities);
		return new ListResponse<>(response.size(), response);
	}
}
