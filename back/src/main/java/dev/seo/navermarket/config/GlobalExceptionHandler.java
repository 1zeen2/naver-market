package dev.seo.navermarket.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import dev.seo.navermarket.common.dto.ErrorResponseDto;

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

    // 4. 데이터베이스 제약 조건 위반 (예: NOT NULL 위반, UNIQUE 제약 조건 위반) - 409 Conflict 또는 400 Bad Request
    // 이 예외는 Service 단에서 MemberRepository.save() 호출 시 발생할 수 있습니다.
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponseDto> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
		String errorMessage = "데이터베이스 제약 조건을 위반했습니다. 이미 존재하는 아이디/이메일이거나 필수 값이 누락되었습니다.";
		String errorCode = "DB_CONSTRAINT_VIOLATION";
		HttpStatus status = HttpStatus.CONFLICT;
		
		// 좀 더 친화적인 메시지로 변환합니다.
		if (ex.getCause() != null && ex.getCause().getMessage().contains("Duplicate entry")) {
			errorMessage = "이미 사용 중인 아이디 또는 이메일입니다.";
			errorCode = "DUPLICATE_ENTRY";
		} else if (ex.getCause() != null && ex.getCause().getMessage().contains("cannot be null")) {
			errorMessage = "필수 정보가 누락되었습니다.";
			errorCode = "MISSING_REQUIRED_FIELD";
		}
		
		ErrorResponseDto errorResponse = ErrorResponseDto.builder()
				.status(status.value())
				.message(errorMessage)
				.code(errorCode)
				.build();
		return new ResponseEntity<>(errorResponse, status);
	}
	
	// 5. 일반 RuntimeException 처리 (로그인 실패 등)
    //    이 핸들러는 IllegalArgumentException, IllegalStateException, DataIntegrityViolationException
    //    등의 더 구체적인 RuntimeException 자식 클래스들이 처리되지 않은 경우에만 잡습니다.
    //    특히 AuthServiceImpl에서 던지는 "사용자를 찾을 수 없습니다." 또는 "비밀번호가 일치하지 않습니다."
    //    와 같은 메시지를 가진 RuntimeException을 여기서 처리하여 401 Unauthorized를 반환합니다.
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDto> handleRuntimeException(RuntimeException ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; // 기본값: 예상치 못한 런타임 예외는 500
        String errorCode = "INTERNAL_SERVER_ERROR";
        String message = "서버에서 알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요."; // 사용자에게 노출할 기본 메시지

        // 로그인 실패 관련 메시지 처리 (AuthService에서 던지는 특정 메시지)
        if (ex.getMessage() != null &&
            (ex.getMessage().contains("사용자를 찾을 수 없습니다.") || ex.getMessage().contains("비밀번호가 일치하지 않습니다."))) {
            status = HttpStatus.UNAUTHORIZED; // 401 Unauthorized
            errorCode = "AUTH_FAILED";
            message = ex.getMessage(); // AuthService에서 던진 구체적인 메시지 사용
        } else {
            // 그 외의 일반적인 RuntimeException은 500으로 처리하고, 상세 메시지는 로그에만 남깁니다.
            System.err.println("Unexpected RuntimeException: " + ex.getMessage()); // 개발자용 로그
            ex.printStackTrace(); // 스택 트레이스도 로그에 남김
        }

        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .status(status.value())
                .message(message)
                .code(errorCode)
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }

    // 6. 그 외 모든 예상치 못한 예외 - 500 Internal Server Error (가장 일반적인 예외)
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