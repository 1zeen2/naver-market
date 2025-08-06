package dev.seo.navermarket.member.exception;

import dev.seo.navermarket.exception.CustomException;
import dev.seo.navermarket.exception.ErrorCode;

/**
 * @brief 현재 비밀번호가 일치하지 않을 때 발생하는 예외입니다.
 */
public class InvalidPasswordException extends CustomException {
	
	private static final long serialVersionUID = 1L;
	
    public InvalidPasswordException() {
        super(ErrorCode.INVALID_CREDENTIALS); // ErrorCode.INVALID_CREDENTIALS 사용
    }

    public InvalidPasswordException(String message) {
        super(ErrorCode.INVALID_CREDENTIALS, new Throwable(message)); // 메시지를 원인으로 전달
    }
    
}