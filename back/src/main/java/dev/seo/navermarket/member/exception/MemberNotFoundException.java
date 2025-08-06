package dev.seo.navermarket.member.exception;

import dev.seo.navermarket.exception.CustomException;
import dev.seo.navermarket.exception.ErrorCode;

/**
 * @brief 회원을 찾을 수 없을 때 발생하는 예외입니다.
 */
public class MemberNotFoundException extends CustomException {
	
	private static final long serialVersionUID = 1L;
	
    public MemberNotFoundException() {
        super(ErrorCode.MEMBER_NOT_FOUND);
    }

    public MemberNotFoundException(String message) {
        super(ErrorCode.MEMBER_NOT_FOUND, new Throwable(message)); // 메시지를 원인으로 전달
    }

}