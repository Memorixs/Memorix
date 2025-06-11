package com.memo.category.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class CategoryResponse implements  Response{
	private Long id;
	private String name;

}
