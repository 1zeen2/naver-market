package dev.seo.navermarket.member.restcontroller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.seo.navermarket.member.dto.LoginRequestDto;
import dev.seo.navermarket.member.dto.LoginResponseDto;
import dev.seo.navermarket.member.dto.SignupRequestDto;
import dev.seo.navermarket.member.dto.SignupResponseDto;
import dev.seo.navermarket.member.service.AuthService;
import dev.seo.navermarket.security.jwt.JwtTokenProvider;
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
    private final JwtTokenProvider jwtTokenProvider;
    
    private static final Logger log = LoggerFactory.getLogger(AuthRestController.class);
    
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
    
    /**
     * @brief 현재 로그인된 사용자 정보를 JWT 토큰을 통해 조회하는 API 엔드포인트입니다.
     * GET /api/auth/me
     * @param authorizationHeader Authorization 헤더 (Bearer JWT_TOKEN)
     * @return ResponseEntity<Map<String, Object>> 사용자 정보 (userId, memberId, userName)와 HTTP 상태 코드
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMyInfo(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        log.info("사용자 정보 조회 요청 수신.");

        // Authorization 헤더가 없거나 'Bearer '로 시작하지 않으면 UNAUTHORIZED 반환
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("Authorization 헤더가 없거나 'Bearer '로 시작하지 않습니다.(토큰 부재)");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "토큰이 유효하지 않습니다."));
        }

        // "Bearer " 접두사 제거하여 순수 토큰 문자열 추출
        String token = authorizationHeader.substring(7);

        // JWT 토큰 유효성 검사
        if (!jwtTokenProvider.validateToken(token)) {
            log.warn("유효하지 않거나 만료된 JWT 토큰입니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "토큰이 유효하지 않거나 만료되었습니다."));
        }

        try {
            // 토큰에서 사용자 정보 추출
            String userId = jwtTokenProvider.extractUserId(token);
            Long memberId = jwtTokenProvider.extractMemberId(token);
            String userName = jwtTokenProvider.extractUserName(token);

            // 추출된 사용자 정보를 Map에 담아 반환
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("memberId", memberId);
            userInfo.put("userId", userId);
            userInfo.put("userName", userName);
            
            log.info("사용자 정보 반환: userId={}", userId);
            return ResponseEntity.ok(userInfo);

        } catch (Exception e) {
            // 토큰 파싱 또는 정보 추출 중 예외 발생 시 INTERNAL_SERVER_ERROR 반환
            log.error("JWT 토큰에서 사용자 정보 추출 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "사용자 정보 처리 중 오류가 발생했습니다."));
        }
    }

}
