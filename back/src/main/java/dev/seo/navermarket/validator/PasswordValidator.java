package dev.seo.navermarket.validator;

import org.springframework.stereotype.Component;

@Component
public class PasswordValidator {
	
	// 영문, 숫자 특수문자 포함 8자리 이상
	private static final String REGEX = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$";
	
	public void validateUserPwd(String userPwd) {
		if (!userPwd.matches(REGEX)) {
			throw new IllegalArgumentException("비밀번호는 영문, 숫자, 특수문자를 포함하여 최소 8자리 이상이어야 합니다.");
		}
	}

}
