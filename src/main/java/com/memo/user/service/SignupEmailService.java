package com.memo.user.service;

import java.util.concurrent.TimeUnit;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.memo.common.exception.CustomException;
import com.memo.common.exception.ExceptionType;
import com.memo.common.properties.MailProperties;
import com.memo.common.util.EmailService;
import com.memo.common.util.EmailUtils;
import com.memo.storage.MailLinkTokenRepository;
import com.memo.storage.TokenRepository;
import com.memo.user.entity.User;
import com.memo.user.repository.UserRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SignupEmailService implements EmailService {
	private final JavaMailSender emailSender;
	private final MailProperties mailProperties;
	private final MailLinkTokenRepository mailLinkTokenStore;
	private final UserRepository userRepository;
	private final TokenRepository tokenRepository;

	@Override
	public void createMessage(MimeMessage message, String to, String subject, String text) throws MessagingException {
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setFrom(mailProperties.getUsername());
		helper.setTo(to);
		helper.setSubject(subject);
		helper.setText(text, true);
	}

	public void sendVerifiedMessage(String email) throws MessagingException {
		MimeMessage message = emailSender.createMimeMessage();

		//토큰 말고 이메일 레디스에 저장해서 만료시간 설정
		String verificationLink = EmailUtils.createVerificationLink(email);
		String text = EmailUtils.createMessageForSignup(verificationLink);
		String sub = EmailUtils.createSubForSignup(email);

		createMessage(message, email, sub, text);
		tokenRepository.save("email;" + email, email, 5, TimeUnit.MINUTES);
		emailSender.send(message);
	}

	public User validateEmailToken(String token) {
		if (tokenRepository.findByKey("email;"+token) == null) {
			throw new CustomException(ExceptionType.EXPIRED_LINK); //프론트가 링크 재요청 페이지 띄우고 백엔드에 다시 요청, 메일 날리는 api따로 만들기
		}
		//만료되지 않았다면
		//user 반환
		mailLinkTokenStore.deleteByEmail(token);
		return userRepository.findByEmailEquals(token);
	}
}
