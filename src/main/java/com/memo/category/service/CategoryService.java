package com.memo.category.service;

import static com.memo.common.exception.ExceptionType.*;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.memo.Resource;
import com.memo.category.dto.CategoryResponse;
import com.memo.category.dto.ListResponse;
import com.memo.category.entity.Category;
import com.memo.category.repository.CategoryRepository;
import com.memo.common.exception.CustomException;
import com.memo.common.exception.ExceptionType;
import com.memo.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {
	private final CategoryRepository categoryRepository;

	public ListResponse<CategoryResponse> findByUser(User user) {
		List<Category> entities = categoryRepository.findByUserId(user.getId());
		List<CategoryResponse> response = Category.entityToDto(entities);
		return new ListResponse<>(response.size(), response);
	}

	public Long save(String name, User user) {
		//중복된 카테고리 안됨
		if(categoryRepository.findByName(name) != null) {
			throw new CustomException(ExceptionType.EXIST_CATEGORY, name);
		}
		Category category = Category.createByUser(name, user);
		Category newCategory = categoryRepository.save(category);
		return newCategory.getId();
	}

	@Transactional
	public void updateById(Long id, String name, User user) {
		Category category = categoryRepository.findByIdAndIsDeletedIsFalse(id)
			.orElseThrow(() -> new CustomException(NOT_FOUND_CATEGORY, id));

		checkAuthorization(user, category);
		//권한 확인 되면 수정
		if(categoryRepository.findByUserIdAndNameAndIsDeletedFalse(user.getId(), name) != null){
			throw new CustomException(DUPLICATED_CATEGORY, name);
		}//유저가 등록한 카테고리중 중복된 name이 있는 지확인
		category.updateName(name);
	}

	private void checkAuthorization(User user, Resource resource) {
		//특정 리소스 수정할 때 권한 검사
		if (!user.getId().equals(resource.getUser().getId())) {
			throw new CustomException(FORBIDDEN, user.getId());
		}
	}
}
