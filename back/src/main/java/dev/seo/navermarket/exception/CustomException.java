package dev.seo.navermarket.exception;

import lombok.Getter;

/**
 * @brief 애플리케이션에서 발생하는 모든 비즈니스 로직 관련 사용자 정의 예외의 기본 클래스입니다.
 * ErrorCode 열거형을 사용하여 구체적인 에러 정보(HTTP 상태, 코드, 메시지)를 포함합니다.
 * 이 예외는 RuntimeException을 상속받아 Unchecked Exception으로 동작하여
 * 메서드 시그니처에 throws 선언을 강제하지 않습니다.
 */
@Getter
public class CustomException extends RuntimeException {
	private static final long serialVersionUID = 1L;
    private final ErrorCode errorCode;

    /**
     * @brief ErrorCode를 인자로 받아 CustomException을 생성합니다.
     * 이 생성자는 주로 비즈니스 로직에서 특정 에러 상황을 나타낼 때 사용됩니다.
     * @param errorCode 발생한 에러의 종류와 상세 정보를 담고 있는 ErrorCode 열거형 값
     */
    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // RuntimeException의 메시지로 ErrorCode의 메시지를 사용
        this.errorCode = errorCode;
    }

    /**
     * @brief ErrorCode와 예외의 원인(Throwable)을 인자로 받아 CustomException을 생성합니다.
     * 이 생성자는 다른 저수준 예외(예: DB 예외)가 발생했을 때 이를 CustomException으로 래핑하여
     * 상위 계층으로 전달할 때 유용합니다.
     * @param errorCode 발생한 에러의 종류와 상세 정보를 담고 있는 ErrorCode 열거형 값
     * @param cause 이 예외의 원인이 되는 Throwable 객체 (원본 예외)
     */
    public CustomException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause); // RuntimeException의 메시지와 원인을 설정
        this.errorCode = errorCode;
    }
}
