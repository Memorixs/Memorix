package com.memo.common.util;

public interface EmailService {
	//지금은 회원가입 인증 메일 전송용 밖에 없지만 나중에 비번 초기화를 위해 사용할 인터페이스
	void sendMessage(String to, String subject, String text);
}
