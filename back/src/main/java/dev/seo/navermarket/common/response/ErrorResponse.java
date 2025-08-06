package dev.seo.navermarket.common.response;

import dev.seo.navermarket.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @brief API 오류 발생 시 클라이언트에 반환되는 표준 응답 DTO 클래스입니다.
 * 오류 메시지, 상태 코드, 그리고 필요한 경우 유효성 검사 실패 상세 정보를 포함합니다.
 */
@Getter
@Builder
public class ErrorResponse {
    private final int status; // HTTP 상태 코드
    private final String code; // 애플리케이션 정의 에러 코드
    private final String message; // 사용자에게 보여줄 에러 메시지
    private final List<FieldErrorDetail> errors; // 유효성 검사 실패 시 상세 에러 목록

    /**
     * @brief ErrorCode를 사용하여 ErrorResponse 객체를 생성합니다.
     * 일반적인 비즈니스 로직 예외 처리 시 사용됩니다.
     * @param errorCode 발생한 에러의 종류와 상세 정보를 담고 있는 ErrorCode 열거형 값
     * @return ErrorResponse 생성된 ErrorResponse 객체
     */
    public static ErrorResponse of(final ErrorCode errorCode) {
        return ErrorResponse.builder()
                .status(errorCode.getStatus().value()) // HttpStatus의 숫자 값
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .errors(List.of()) // 에러 상세 목록은 비워둠
                .build();
    }

    /**
     * @brief ErrorCode와 BindingResult를 사용하여 ErrorResponse 객체를 생성합니다.
     * 주로 @Valid 또는 @Validated 유효성 검사 실패 시 사용되어, 어떤 필드에서 어떤 에러가 발생했는지 상세 정보를 제공합니다.
     * @param errorCode 발생한 에러의 종류와 상세 정보를 담고 있는 ErrorCode 열거형 값 (예: INVALID_INPUT_VALUE)
     * @param bindingResult 유효성 검사 실패에 대한 상세 정보를 담고 있는 BindingResult 객체
     * @return ErrorResponse 생성된 ErrorResponse 객체
     */
    public static ErrorResponse of(final ErrorCode errorCode, final BindingResult bindingResult) {
        return ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .errors(FieldErrorDetail.of(bindingResult)) // 유효성 검사 실패 상세 정보 포함
                .build();
    }

    /**
     * @brief 유효성 검사 실패 시 특정 필드에 대한 에러 상세 정보를 나타내는 내부 클래스입니다.
     */
    @Getter
    @Builder
    public static class FieldErrorDetail {
        private final String field; // 에러가 발생한 필드명
        private final String value; // 에러가 발생한 필드의 입력 값
        private final String reason; // 에러 발생 이유 (메시지)

        /**
         * @brief BindingResult로부터 FieldErrorDetail 목록을 생성합니다.
         * @param bindingResult 유효성 검사 실패에 대한 상세 정보를 담고 있는 BindingResult 객체
         * @return List<FieldErrorDetail> 각 필드 에러에 대한 상세 정보 목록
         */
        public static List<FieldErrorDetail> of(final BindingResult bindingResult) {
            final List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            return fieldErrors.stream()
                    .map(error -> FieldErrorDetail.builder()
                            .field(error.getField())
                            .value(error.getRejectedValue() == null ? "" : error.getRejectedValue().toString())
                            .reason(error.getDefaultMessage())
                            .build())
                    .collect(Collectors.toList());
        }
    }
}
