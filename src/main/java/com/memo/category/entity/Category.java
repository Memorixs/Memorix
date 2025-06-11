package com.memo.category.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.memo.category.dto.CategoryResponse;
import com.memo.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Category {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(length = 20)
	private String name;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private Boolean isDeleted;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	public static CategoryResponse entityToDto(Category entity) {
		return new CategoryResponse(entity.getId(), entity.getName());
	}

	public static List<CategoryResponse> entityToDto(List<Category> entities) {
		return entities.stream()
			.map(Category::entityToDto)
			.toList();
	}
}
