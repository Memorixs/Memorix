package com.memo.category.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class ListResponse<T extends Response> {
	private int total;
	private List<T> response;
}
