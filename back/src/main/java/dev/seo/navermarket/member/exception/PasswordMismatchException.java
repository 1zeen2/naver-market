package dev.seo.navermarket.member.exception;

import dev.seo.navermarket.exception.CustomException;
import dev.seo.navermarket.exception.ErrorCode;

/**
 * @brief 새 비밀번호와 확인 비밀번호가 일치하지 않을 때 발생하는 예외입니다.
 */
public class PasswordMismatchException extends CustomException {
	
	private static final long serialVersionUID = 1L;
	
    public PasswordMismatchException() {
        super(ErrorCode.PASSWORD_MISMATCH);
    }

    public PasswordMismatchException(String message) {
        super(ErrorCode.PASSWORD_MISMATCH, new Throwable(message)); // 메시지를 원인으로 전달
    }

}