package dev.seo.navermarket.common.exception;

import lombok.Getter;

/**
 * @file BaseException.java
 * @brief 모든 사용자 정의 예외의 기본이 되는 추상 클래스입니다.
 * 공통적인 에러 코드 필드를 포함합니다.
 */
@Getter
public abstract class BaseException extends RuntimeException {
    // serialVersionUID 추가: 직렬화 호환성을 위해 명시적으로 선언
    private static final long serialVersionUID = 1L; 

    private final String errorCode;

    public BaseException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BaseException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
