package com.memo.category.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.memo.category.dto.CategoryResponse;
import com.memo.category.dto.ListResponse;
import com.memo.category.service.CategoryService;
import com.memo.common.security.CustomUserDetails;
import com.memo.user.entity.User;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {
	private final CategoryService categoryService;

	@GetMapping
	public ResponseEntity<ListResponse<CategoryResponse>> findAll(@AuthenticationPrincipal CustomUserDetails userDetails) {
		User user = userDetails.getUser();
		ListResponse<CategoryResponse> response = categoryService.findByUser(user);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping
	public ResponseEntity<Long> save(@RequestParam String name, @AuthenticationPrincipal CustomUserDetails userDetails) {
		User user = userDetails.getUser();
		Long response = categoryService.save(name, user);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{id}")
	public ResponseEntity<String> updateById(@PathVariable Long id, @RequestParam String name,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		User user = userDetails.getUser();
		categoryService.updateById(id, name, user);
		return ResponseEntity.ok("수정 완료");
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteById(@PathVariable Long id,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		User user = userDetails.getUser();
		categoryService.deleteById(user, id);
		return ResponseEntity.ok("삭제 완료");
	}
}
