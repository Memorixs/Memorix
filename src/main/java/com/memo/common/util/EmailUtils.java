package com.memo.common.util;

public class EmailUtils {

	public static String createMessageForSignup(String verificationLink) {
		StringBuilder msgOfEmail = new StringBuilder();
		msgOfEmail.append("<!DOCTYPE html>");
		msgOfEmail.append("<html lang='ko'>");
		msgOfEmail.append("<head>");
		msgOfEmail.append("<meta charset='UTF-8'>");
		msgOfEmail.append("<title>회원가입 이메일 인증</title>");
		msgOfEmail.append("</head>");
		msgOfEmail.append("<body style='font-family: Arial, sans-serif; background-color: #f8f8f8; padding: 40px;'>");
		msgOfEmail.append("<div style='max-width: 600px; margin: auto; background-color: #ffffff; border-radius: 8px; padding: 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.05);'>");

		msgOfEmail.append("<h2 style='color: #333;'>안녕하세요, <strong>Memorix</strong>입니다.</h2>");
		msgOfEmail.append("<p style='font-size: 16px; color: #555;'>회원가입을 위해 이메일 주소 인증이 필요합니다.</p>");
		msgOfEmail.append("<p style='font-size: 16px; color: #555;'>아래 버튼을 클릭하시면 인증이 완료됩니다.</p>");

		msgOfEmail.append("<div style='margin: 30px 0; text-align: center;'>");
		msgOfEmail.append("<a href='").append(verificationLink).append("' ");
		msgOfEmail.append("style='background-color: #2b7cff; color: #fff; padding: 14px 24px; border-radius: 6px; text-decoration: none; font-size: 16px;'>");
		msgOfEmail.append("이메일 인증하기</a>");
		msgOfEmail.append("</div>");

		msgOfEmail.append("<p style='font-size: 14px; color: #888;'>이 링크는 발송 후 <strong>5분간 유효</strong>합니다.<br>");
		msgOfEmail.append("요청하지 않으셨다면 이 메일을 무시하셔도 됩니다.</p>");

		msgOfEmail.append("<p style='margin-top: 40px; font-size: 14px; color: #aaa;'>감사합니다.<br>Memorix 드림</p>");
		msgOfEmail.append("</div>");
		msgOfEmail.append("</body>");
		msgOfEmail.append("</html>");

		return msgOfEmail.toString();
	}

	public static String createVerificationLink(String token) {
		StringBuffer sb = new StringBuffer();
		sb.append(UtilString.EMAIL_AUTH_URL.value());
		sb.append(token);
		return sb.toString();
	}

	public static String createSubForSignup(String email) {
		StringBuffer sub = new StringBuffer();
		sub.append("[Memorix]").append(" ");
		sub.append(email).append("님 환영합니다. "); //여기에 username을 넣을까 하다가 이것도 사용자 정보라서 다른 메일로 간다면 회원 정보가 노출되는 것
		sub.append("회원가입 완료를 위해 확인해주세요.");
		return sub.toString();
	}
}
