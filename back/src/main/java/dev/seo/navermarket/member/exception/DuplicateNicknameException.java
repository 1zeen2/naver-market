package dev.seo.navermarket.member.exception;

import dev.seo.navermarket.exception.CustomException;
import dev.seo.navermarket.exception.ErrorCode;

/**
 * @brief 닉네임 중복 시 발생하는 예외입니다.
 */
public class DuplicateNicknameException extends CustomException {
	
	private static final long serialVersionUID = 1L;
	
    public DuplicateNicknameException() {
        super(ErrorCode.DUPLICATE_NICKNAME);
    }

    public DuplicateNicknameException(String message) {
        super(ErrorCode.DUPLICATE_NICKNAME, new Throwable(message)); // 메시지를 원인으로 전달
    }
    
}