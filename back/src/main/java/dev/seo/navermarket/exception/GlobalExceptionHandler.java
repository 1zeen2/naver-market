package dev.seo.navermarket.exception;

import org.springframework.dao.DataIntegrityViolationException; // ✨ DB 제약 조건 위반 예외 임포트
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. @Valid 검증 실패 시 발생하는 예외 처리 (400 Bad Request)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // 2. 비즈니스 로직 유효성 검사 실패 (예: 비밀번호 정책 위반) - 400 Bad Request
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // 3. 비즈니스 규칙 위반 (예: 이미 존재하는 아이디/이메일) - 409 Conflict
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    // 4. 데이터베이스 제약 조건 위반 (예: NOT NULL 위반, UNIQUE 제약 조건 위반) - 409 Conflict 또는 400 Bad Request
    // 이 예외는 Service 단에서 MemberRepository.save() 호출 시 발생할 수 있습니다.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        // 좀 더 친화적인 메시지로 변환할 수 있습니다.
        // ex.getCause().getMessage()를 통해 원인 분석하여 특정 메시지 제공 가능
        String errorMessage = "데이터베이스 제약 조건을 위반했습니다. 이미 존재하는 아이디/이메일이거나 필수 값이 누락되었습니다.";
        if (ex.getCause() != null && ex.getCause().getMessage().contains("Duplicate entry")) {
            errorMessage = "이미 사용 중인 아이디 또는 이메일입니다.";
        } else if (ex.getCause() != null && ex.getCause().getMessage().contains("cannot be null")) {
            errorMessage = "필수 정보가 누락되었습니다.";
        }
        return new ResponseEntity<>(errorMessage, HttpStatus.CONFLICT); // 충돌 또는 잘못된 요청
    }

    // 5. 그 외 모든 예상치 못한 예외 - 500 Internal Server Error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
    	// 나중에는  **로깅 라이브러리(예: SLF4J + Logback)**로 변경해야 함
    	ex.printStackTrace();
        return new ResponseEntity<>("서버에서 알 수 없는 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}