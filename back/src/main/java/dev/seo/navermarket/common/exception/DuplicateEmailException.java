package dev.seo.navermarket.common.exception;


/**
 * @file DuplicateEmailException.java
 * @brief 회원가입 시 이메일이 이미 존재할 때 발생하는 예외입니다.
 */
public class DuplicateEmailException extends BaseException {
    private static final long serialVersionUID = 1L; 

    public DuplicateEmailException(String message) {
        super(message, "DUPLICATE_EMAIL");
    }

    public DuplicateEmailException(String message, Throwable cause) {
        super(message, "DUPLICATE_EMAIL", cause);
    }
}