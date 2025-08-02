package dev.seo.navermarket.common.exception;

/**
 * @file InvalidTokenException.java
 * @brief JWT 토큰이 유효하지 않거나 만료되었을 때 발생하는 예외입니다.
 */
public class InvalidTokenException extends BaseException {
    private static final long serialVersionUID = 1L; 

    public InvalidTokenException(String message) {
        super(message, "INVALID_TOKEN");
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, "INVALID_TOKEN", cause);
    }
}