package dev.seo.navermarket.common.exception;

/**
 * @file DuplicateUserIdException.java
 * @brief 회원가입 시 아이디가 이미 존재할 때 발생하는 예외입니다.
 */
public class DuplicateUserIdException extends BaseException {
    private static final long serialVersionUID = 1L; 

    public DuplicateUserIdException(String message) {
        super(message, "DUPLICATE_USER_ID");
    }

    public DuplicateUserIdException(String message, Throwable cause) {
        super(message, "DUPLICATE_USER_ID", cause);
    }
}