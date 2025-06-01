package com.memo.user.service;

import java.util.Date;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.memo.common.properties.MailProperties;
import com.memo.common.util.EmailService;
import com.memo.common.util.EmailUtils;
import com.memo.storage.MailLinkToken;
import com.memo.storage.MailLinkTokenRepository;
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

		// String token = createToken(email);
		//토큰 말고 이메일 레디스에 저장해서 만료시간 설정
		String verificationLink = EmailUtils.createVerificationLink(email);
		String text = EmailUtils.createMessageForSignup(verificationLink);
		String sub = EmailUtils.createSubForSignup(email);

		createMessage(message, email, sub, text);
		mailLinkTokenStore.save(new MailLinkToken(email, new Date(System.currentTimeMillis() + 5 * 60 * 1000))); //5분
		emailSender.send(message);
	}

	public User validateEmailToken(String token) {
		Date expired = mailLinkTokenStore.findByEmail(token).getExpired();
		if (expired.before(new Date())) {
			throw new RuntimeException("만료된 링크입니다. 다시 요청해주세요."); //프론트가 링크 재요청 페이지 띄우고 백엔드에 다시 요청, 메일 날리는 api따로 만들기
		}
		//만료되지 않았다면
		//user 반환
		mailLinkTokenStore.deleteByEmail(token);
		return userRepository.findByEmailEquals(token);
	}
}
