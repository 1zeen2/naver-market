package dev.seo.navermarket.common.exception;

import dev.seo.navermarket.common.response.ErrorResponse;
import dev.seo.navermarket.exception.CustomException;
import dev.seo.navermarket.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * @brief 애플리케이션 전반의 예외를 중앙에서 처리하는 핸들러입니다.
 * 발생하는 다양한 예외들을 캐치하여 일관된 에러 응답 형식(ErrorResponse)을 반환합니다.
 */
@RestControllerAdvice // 모든 @Controller 또는 @RestController에 대한 전역 예외 처리
@Slf4j // 로깅을 위한 Lombok 어노테이션
public class GlobalExceptionHandler {

    /**
     * @brief CustomException 처리 핸들러
     * 우리가 정의한 비즈니스 로직 예외를 처리합니다.
     * @param e 발생한 CustomException 객체
     * @return ResponseEntity<ErrorResponse> 일관된 에러 응답
     */
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(final CustomException e) {
        log.error("handleCustomException: {}", e.getErrorCode().getMessage(), e); // 에러 로그 기록
        final ErrorCode errorCode = e.getErrorCode();
        final ErrorResponse response = ErrorResponse.of(errorCode); // ErrorCode로부터 ErrorResponse 생성
        return new ResponseEntity<>(response, errorCode.getStatus()); // ErrorCode에 정의된 HTTP 상태 코드 사용
    }

    /**
     * @brief @Valid 또는 @Validated를 통한 유효성 검사 실패 시 발생하는 예외 처리 핸들러
     * 주로 DTO 유효성 검사 실패 시 발생합니다.
     * @param e 발생한 MethodArgumentNotValidException 객체
     * @return ResponseEntity<ErrorResponse> 일관된 에러 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        log.error("handleMethodArgumentNotValidException: {}", e.getMessage(), e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e.getBindingResult()); // 유효성 검사 실패 상세 정보 포함
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * @brief @ModelAttribute 바인딩 실패 시 발생하는 예외 처리 핸들러
     * @param e 발생한 BindException 객체
     * @return ResponseEntity<ErrorResponse> 일관된 에러 응답
     */
    @ExceptionHandler(BindException.class)
    protected ResponseEntity<ErrorResponse> handleBindException(final BindException e) {
        log.error("handleBindException: {}", e.getMessage(), e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e.getBindingResult());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * @brief enum 타입 불일치 등으로 인한 인자 타입 미스매치 예외 처리 핸들러
     * @param e 발생한 MethodArgumentTypeMismatchException 객체
     * @return ResponseEntity<ErrorResponse> 일관된 에러 응답
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException e) {
        log.error("handleMethodArgumentTypeMismatchException: {}", e.getMessage(), e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_TYPE_VALUE);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * @brief 지원하지 않는 HTTP 메서드 호출 시 발생하는 예외 처리 핸들러
     * @param e 발생한 HttpRequestMethodNotSupportedException 객체
     * @return ResponseEntity<ErrorResponse> 일관된 에러 응답
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(final HttpRequestMethodNotSupportedException e) {
        log.error("handleHttpRequestMethodNotSupportedException: {}", e.getMessage(), e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED);
        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * @brief 그 외 모든 예상치 못한 예외 처리 핸들러
     * 애플리케이션에서 발생할 수 있는 모든 일반적인 예외를 처리합니다.
     * @param e 발생한 Exception 객체
     * @return ResponseEntity<ErrorResponse> 일관된 에러 응답
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(final Exception e) {
        log.error("handleException: {}", e.getMessage(), e); // 스택 트레이스 포함하여 상세 로그 기록
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
