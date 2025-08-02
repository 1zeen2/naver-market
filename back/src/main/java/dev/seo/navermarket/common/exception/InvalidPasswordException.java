package dev.seo.navermarket.common.exception;

/**
 * @file InvalidPasswordException.java
 * @brief 비밀번호가 일치하지 않거나 유효하지 않을 때 발생하는 예외입니다.
 */
public class InvalidPasswordException extends BaseException {

	private static final long serialVersionUID = 1L;
	
	public InvalidPasswordException(String message) {
		super(message, "INVALID_PASSWORD");
	}

	public InvalidPasswordException(String message, Throwable cause) {
		super(message, "INVALID_PASSWORD", cause);
	}

}
