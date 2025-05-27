package com.memo.user.entity;

import lombok.Getter;

@Getter
public enum LoginType {
	//아직은 두개밖에 없지만 확장될 수 도 있음
	KAKAO, NATIVE, GOOGLE
}
