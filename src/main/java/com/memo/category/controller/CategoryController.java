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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Category API", description = "카테고리 도메인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {
	private final CategoryService categoryService;

	@Operation(
		summary = "유저가 등록한 카테고리 조회",
		description = "유저가 등록한 카테고리 전체 조회 API",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "성공",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(implementation = ListResponse.class)
				)
			)
		}
	)
	@GetMapping
	public ResponseEntity<ListResponse<CategoryResponse>> findAll(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		User user = userDetails.getUser();
		ListResponse<CategoryResponse> response = categoryService.findByUser(user);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}


	@Operation(
		summary = "카테고리 등록",
		description = "쿼리 파라미터로 전달한 값으로 카테고리 등록하는 API",
		parameters = @Parameter(
			name = "name",
			description = "등록할 카테고리 이름",
			required = true,
			in = ParameterIn.QUERY
		),
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "성공"
			),
			@ApiResponse(
				responseCode = "400",
				description = "중복된 카테고리 이름이면 발생",
				content = @Content(
					examples = @ExampleObject(
						value = "실패하였습니다."
					)
				)
			),
			@ApiResponse(
				responseCode = "500",
				description = "서버 오류",
				content = @Content(
					examples = @ExampleObject(
						value = "실패하였습니다."
					)
				)
			)

		}
	)
	@PostMapping
	public ResponseEntity<Long> save(@RequestParam String name,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		User user = userDetails.getUser();
		Long response = categoryService.save(name, user);
		return ResponseEntity.ok(response);
	}

	@Operation(
		summary = "카테고리 수정",
		description = "식별자 id인 카테고리의 name을 파라미터로 전달한 name으로 변경하는 API",
		parameters = {
			@Parameter(
			name = "name",
			description = "카테고리가 name으로 수정된다",
			required = true,
			in = ParameterIn.QUERY
			),
			@Parameter(
				name = "id",
				description = "변경하려는 카테고리 식별자 id",
				required = true,
				in = ParameterIn.PATH
			)
		},
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "성공"
			),
			@ApiResponse(
				responseCode = "400",
				description = "중복된 카테고리 이름이면 발생",
				content = @Content(
					examples = @ExampleObject(
						value = "실패하였습니다."
					)
				)
			),
			@ApiResponse(
				responseCode = "403",
				description = "식별자 id를 가진 카테고리를 수정할 권한이 없는 유저의 요청입니다.",
				content = @Content(
					examples = @ExampleObject(
						value = "실패하였습니다."
					)
				)
			),
			@ApiResponse(
				responseCode = "500",
				description = "서버 오류",
				content = @Content(
					examples = @ExampleObject(
						value = "실패하였습니다."
					)
				)
			)
		}
	)
	@PatchMapping("/{id}")
	public ResponseEntity<String> updateById(@PathVariable Long id, @RequestParam String name,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		User user = userDetails.getUser();
		categoryService.updateById(id, name, user);
		return ResponseEntity.ok("수정 완료");
	}

	@Operation(
		summary = "카테고리 삭제",
		description = "식별자 id인 카테고리를 삭제하는 API",
		parameters = @Parameter(
			name = "id",
			description = "삭제하려는 카테고리 id",
			required = true,
			in = ParameterIn.PATH
		),
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "성공"
			),
			@ApiResponse(
				responseCode = "403",
				description = "식별자 id를 가진 카테고리를 삭제할 권한이 없는 유저의 요청입니다.",
				content = @Content(
					examples = @ExampleObject(
						value = "실패하였습니다."
					)
				)
			),
			@ApiResponse(
				responseCode = "500",
				description = "서버 오류",
				content = @Content(
					examples = @ExampleObject(
						value = "실패하였습니다."
					)
				)
			)
		}
	)
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteById(@PathVariable Long id,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		User user = userDetails.getUser();
		categoryService.deleteById(user, id);
		return ResponseEntity.ok("삭제 완료");
	}
}
