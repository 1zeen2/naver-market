package dev.seo.navermarket.member.exception;

import dev.seo.navermarket.exception.CustomException;
import dev.seo.navermarket.exception.ErrorCode;

/**
 * @brief 사용자 ID 중복 시 발생하는 예외입니다.
 */
public class DuplicateUserIdException extends CustomException {
	
	private static final long serialVersionUID = 1L;

	public DuplicateUserIdException() {
        super(ErrorCode.DUPLICATE_USER_ID);
    }

    public DuplicateUserIdException(String message) {
        super(ErrorCode.DUPLICATE_USER_ID, new Throwable(message)); // 메시지를 원인으로 전달
    }
    
}