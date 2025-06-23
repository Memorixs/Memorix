package com.memo.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SortType {
	CREATED_AT_ASC("오래된순"),
	CREATED_AT_DESC("최신 순"),
	KO_ASC("가다나순"),
	EN_ASC("알파벳순");

	private final String label;
}
