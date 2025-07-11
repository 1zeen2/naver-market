package dev.seo.navermarket.restcontroller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.seo.navermarket.dto.ErrorResponseDto;
import dev.seo.navermarket.dto.LoginRequestDto;
import dev.seo.navermarket.dto.LoginResponseDto;
import dev.seo.navermarket.dto.SignupRequestDto;
import dev.seo.navermarket.dto.SignupResponseDto;
import dev.seo.navermarket.service.AuthService;
import lombok.RequiredArgsConstructor;

/**
 * @file AuthRestController.java
 * @brief 인증(로그인 및 회원가입) 관련 REST API 요청을 처리하는 컨트롤러 클래스입니다.
 * 프론트엔드로부터의 HTTP 요청을 받아 인증 로직을 수행하고 응답합니다.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthService authService;
    
    /**
     * @brief 사용자 회원가입을 처리하는 API 엔드포인트입니다.
     * POST /api/auth/signup
     * @param requestDto 회원가입 요청 데이터
     * @return ResponseEntity<SignupResponseDto> 회원가입 성공 응답 DTO와 HTTP 상태 코드 (201 Created)
     */
    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDto> signup(@RequestBody SignupRequestDto requestDto) {
        SignupResponseDto responseDto = authService.signup(requestDto);
        
        // 201 Created 상태 코드와 함께 응답 DTO 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * @brief 사용자 로그인을 처리하는 API 엔드포인트입니다.
     * POST /api/auth/login
     * @param loginRequestDto 로그인 요청 정보 (사용자 ID, 비밀번호)
     * @return ResponseEntity<LoginResponseDto> 로그인 성공 시 반환되는 정보와 HTTP 상태 코드 (200 OK)
     * @throws RuntimeException 로그인 실패 시 AuthServiceImpl에서 발생하며 GlobalExceptionHandler에서 처리됩니다.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        LoginResponseDto response = authService.login(loginRequestDto);
        return ResponseEntity.ok(response); // 200 OK 상태 코드와 함께 로그인 응답 반환
    }
}
