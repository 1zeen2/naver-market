package dev.seo.navermarket.member.exception;

import dev.seo.navermarket.exception.CustomException;
import dev.seo.navermarket.exception.ErrorCode;

/**
 * @brief 이메일 중복 시 발생하는 예외입니다.
 */
public class DuplicateEmailException extends CustomException {
	
	private static final long serialVersionUID = 1L;
	
    public DuplicateEmailException() {
        super(ErrorCode.DUPLICATE_EMAIL);
    }

    public DuplicateEmailException(String message) {
        super(ErrorCode.DUPLICATE_EMAIL, new Throwable(message)); // 메시지를 원인으로 전달
    }

}