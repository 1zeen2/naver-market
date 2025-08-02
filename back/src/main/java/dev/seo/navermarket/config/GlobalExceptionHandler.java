package dev.seo.navermarket.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import dev.seo.navermarket.common.dto.ErrorResponseDto;
import dev.seo.navermarket.common.exception.DuplicateEmailException;
import dev.seo.navermarket.common.exception.DuplicateUserIdException;
import dev.seo.navermarket.common.exception.InvalidPasswordException;
import dev.seo.navermarket.common.exception.InvalidTokenException;
import dev.seo.navermarket.common.exception.UserNotFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * @file GlobalExceptionHandler.java
 * @brief 애플리케이션 전역에서 발생하는 예외를 처리하는 클래스입니다.
 * 컨트롤러에서 발생하는 다양한 예외를 가로채어 일관된 ErrorResponseDto를 반환합니다.
 */
@RestControllerAdvice // 모든 @Controller 또는 @RestController에서 발생하는 예외를 처리함.
public class GlobalExceptionHandler {

    // 1. @Valid 검증 실패 시 발생하는 예외 처리 (400 Bad Request)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        // 유효성 검사 오류는 Map 형태로 반환하므로, ErrorResponseDto의 message에 직렬화하여 담음.
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
        		.status(HttpStatus.BAD_REQUEST.value())
        		.message("유효성 검사 실패: " + errors.toString()) // Map을 문자열로 변환하여 메시지에 포함
        		.code("VALIDATION_FAILED")
        		.build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // 2. 비즈니스 로직 유효성 검사 실패 (예: 비밀번호 정책 위반) - 400 Bad Request
    //	  RuntimeException의 자식 클래스이므로, RuntimeException 핸들러보다 먼저 처리됨
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(IllegalArgumentException ex) {
    	ErrorResponseDto errorResponse = ErrorResponseDto.builder()
    			.status(HttpStatus.BAD_REQUEST.value())
    			.message(ex.getMessage())
    			.code("INVALID_ARGUMENT")
    			.build();
    	return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // 3. 비즈니스 규칙 위반 (예: 이미 존재하는 아이디/이메일) - 409 Conflict
	//  RuntimeException의 자식 클래스이므로, RuntimeException 핸들러보다 먼저 처리됩니다.
    @ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ErrorResponseDto> handleIllegalStateException(IllegalStateException ex) {
    	ErrorResponseDto errorResponse = ErrorResponseDto.builder()
    			.status(HttpStatus.CONFLICT.value())
	            .message(ex.getMessage())
	            .code("CONFLICT_STATE")
	            .build();
    	return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
	}
    
    // 4.1. 사용자 찾을 수 없음 - 404 Not Found (또는 401 Unauthorized for login)
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFoundException(UserNotFoundException ex) {
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .status(HttpStatus.NOT_FOUND.value()) // 사용자를 찾을 수 없으므로 404
                .message(ex.getMessage())
                .code(ex.getErrorCode())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // 4.2. 비밀번호 불일치/유효하지 않음 - 401 Unauthorized
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidPasswordException(InvalidPasswordException ex) {
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .status(HttpStatus.UNAUTHORIZED.value()) // 인증 실패이므로 401
                .message(ex.getMessage())
                .code(ex.getErrorCode())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // 4.3. 아이디 중복 - 409 Conflict
    @ExceptionHandler(DuplicateUserIdException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicateUserIdException(DuplicateUserIdException ex) {
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .status(HttpStatus.CONFLICT.value())
                .message(ex.getMessage())
                .code(ex.getErrorCode())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    // 4.4. 이메일 중복 - 409 Conflict
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicateEmailException(DuplicateEmailException ex) {
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .status(HttpStatus.CONFLICT.value())
                .message(ex.getMessage())
                .code(ex.getErrorCode())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    // 4.5. JWT 토큰 유효성 실패 - 401 Unauthorized
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidTokenException(InvalidTokenException ex) {
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .status(HttpStatus.UNAUTHORIZED.value()) // 토큰 인증 실패이므로 401
                .message(ex.getMessage())
                .code(ex.getErrorCode())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // 5. 데이터베이스 제약 조건 위반 (예: NOT NULL 위반, UNIQUE 제약 조건 위반) - 409 Conflict 또는 400 Bad Request
    //    DuplicateUserIdException, DuplicateEmailException이 먼저 처리되므로,
    //    다른 종류의 DataIntegrityViolationException (예: NOT NULL 위반)을 처리합니다.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String errorMessage = "데이터베이스 제약 조건을 위반했습니다. 필수 값이 누락되었거나 형식이 올바르지 않습니다.";
        String errorCode = "DB_CONSTRAINT_VIOLATION";
        HttpStatus status = HttpStatus.BAD_REQUEST; // 제약 조건 위반은 주로 400 또는 409

        // 좀 더 친화적인 메시지로 변환합니다.
        // Duplicate entry는 이제 DuplicateUserIdException/DuplicateEmailException에서 처리될 가능성이 높습니다.
        if (ex.getCause() != null && ex.getCause().getMessage().contains("cannot be null")) {
            errorMessage = "필수 정보가 누락되었습니다.";
            errorCode = "MISSING_REQUIRED_FIELD";
        } else {
            // 그 외의 DataIntegrityViolationException (예: 너무 긴 문자열, 잘못된 타입 등)
            // 상세 메시지는 로그에만 남기고 사용자에게는 일반적인 메시지 제공
            System.err.println("DataIntegrityViolationException: " + ex.getMessage());
        }

        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .status(status.value())
                .message(errorMessage)
                .code(errorCode)
                .build();
        return new ResponseEntity<>(errorResponse, status);
    }
	
    // 6. 일반 RuntimeException 처리 (예상치 못한 런타임 예외)
    //    이제 로그인 실패 관련 메시지는 UserNotFoundException, InvalidPasswordException에서 처리되므로,
    //    이 핸들러는 위의 모든 특정 RuntimeException 자식 클래스들이 처리되지 않은 경우에만 잡습니다.
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDto> handleRuntimeException(RuntimeException ex) {
        // 예상치 못한 런타임 예외는 500 Internal Server Error로 처리
        System.err.println("Unexpected RuntimeException: " + ex.getMessage()); // 개발자용 로그
        ex.printStackTrace(); // 스택 트레이스도 로그에 남김 (프로덕션에서는 로깅 시스템 사용)

        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("서버에서 알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
                .code("INTERNAL_SERVER_ERROR")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 7. 그 외 모든 예상치 못한 예외 - 500 Internal Server Error (가장 일반적인 예외)
    //    이 핸들러는 위의 모든 특정 예외 핸들러들이 잡지 못한 모든 Exception을 처리합니다.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex) {
        ex.printStackTrace(); // 서버 로그에 스택 트레이스 출력
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("서버에서 알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
                .code("UNKNOWN_ERROR")
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}