package com.memo.common.util;

import com.memo.user.entity.User;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

public interface EmailService {
	//지금은 회원가입 인증 메일 전송용 밖에 없지만 나중에 비번 초기화를 위해 사용할 인터페이스
	void createMessage(MimeMessage message, String to, String subject, String text) throws MessagingException;

	// void sendVerifiedMessage(String email) throws MessagingException;

	// User validateEmailToken(String token) throws MessagingException;
}

