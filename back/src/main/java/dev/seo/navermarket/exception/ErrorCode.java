// src/main/java/dev/seo/navermarket/exception/ErrorCode.java
package dev.seo.navermarket.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * @brief 애플리케이션에서 발생할 수 있는 모든 에러 코드와 해당 HTTP 상태 코드를 정의합니다.
 * 각 에러는 고유한 비즈니스 코드, HTTP 상태, 그리고 사용자 친화적인 메시지를 가집니다.
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Common Errors (Cxxx)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "유효하지 않은 입력 값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "허용되지 않은 HTTP 메서드입니다."),
    HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "C003", "접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "서버 내부 오류가 발생했습니다."),
    NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, "C005", "아직 구현되지 않은 기능입니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C006", "유효하지 않은 타입 값입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C007", "요청한 리소스를 찾을 수 없습니다."), // 예: 존재하지 않는 URL

    // Member Errors (Mxxx)
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_USER_ID(HttpStatus.CONFLICT, "M002", "이미 사용 중인 ID입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "M003", "이미 사용 중인 닉네임 입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "M004", "이미 등록되어 있는 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "M005", "아이디 또는 비밀번호가 일치하지 않습니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "M006", "비밀번호와 확인 비밀번호가 일치하지 않습니다."),
    MEMBER_ALREADY_ACTIVE(HttpStatus.CONFLICT, "M007", "이미 본인 인증이 완료된 사용자입니다."), // 예: 이메일 인증 완료

    // Token Errors (Txxx)
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "T001", "유효하지 않은 Access Token입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "T002", "유효하지 않은 Refresh Token입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "T003", "Refresh Token을 찾을 수 없습니다."),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "T004", "저장된 Refresh Token과 일치하지 않습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "T005", "토큰이 만료되었습니다."),
    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "T006", "지원되지 않는 토큰입니다."),
    MALFORMED_TOKEN(HttpStatus.UNAUTHORIZED, "T007", "잘못된 형식의 토큰입니다."),
    TOKEN_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "T008", "토큰 서명이 유효하지 않습니다."),
    TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "T009", "토큰이 누락되었습니다."),

    // Product/Item Errors (Pxxx) - 예시
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "상품을 찾을 수 없습니다."),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "P002", "재고가 부족합니다."),

    // Order Errors (Oxxx) - 예시
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "주문을 찾을 수 없습니다."),
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "O002", "유효하지 않은 주문 상태입니다.");


    private final HttpStatus status; // HTTP 상태 코드 (예: 400 Bad Request, 404 Not Found)
    private final String code;       // 비즈니스 로직 에러 코드 (예: M001, T002)
    private final String message;    // 사용자에게 보여줄 에러 메시지
}
