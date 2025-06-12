package com.memo.note.entity;

import java.time.LocalDateTime;

import com.memo.Resource;
import com.memo.category.entity.Category;
import com.memo.user.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Note implements Resource {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String title;
	private String content;
	private boolean isBookmarked;
	private boolean know;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private boolean isDeleted;
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;
	@ManyToOne(fetch = FetchType.LAZY)
	private Category category;
}
