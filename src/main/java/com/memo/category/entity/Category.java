package com.memo.category.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.memo.Resource;
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
public class Category implements Resource {
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

	private Category(String name, User user){
		this.name = name;
		this.createdAt = LocalDateTime.now(); //timeZone을 서울로 설정했으므로 서울 날짜로 들어감
		this.updatedAt = LocalDateTime.now();
		this.isDeleted = false;
		this.user = user;
	}

	public static Category createByUser(String name, User user) {
		return new Category(name, user);
	}

	public void updateName(String name) {
		this.name = name;
	}
}
