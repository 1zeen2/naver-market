package dev.seo.navermarket.common.exception;

/**
 * @file DuplicateUserIdException.java
 * @brief 회원가입 시 닉네임이 이미 존재할 때 발생하는 예외입니다.
 */
public class DuplicateNicknameException extends BaseException {
    private static final long serialVersionUID = 1L; 

    public DuplicateNicknameException(String message) {
        super(message, "DUPLICATE_NICKNAME");
    }

    public DuplicateNicknameException(String message, Throwable cause) {
        super(message, "DUPLICATE_NICKNAME", cause);
    }
}