package dev.seo.navermarket.common.exception;


/**
 * @file UserNotFoundException.java
 * @brief 사용자를 찾을 수 없을 때 발생하는 예외입니다.
 */
public class UserNotFoundException extends BaseException {
    // serialVersionUID 추가
    private static final long serialVersionUID = 1L; 

    public UserNotFoundException(String message) {
        super(message, "USER_NOT_FOUND");
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, "USER_NOT_FOUND", cause);
    }
}