package dev.seo.navermarket.auth.restcontroller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.seo.navermarket.auth.dto.LoginRequestDto;
import dev.seo.navermarket.auth.service.AuthService;
import dev.seo.navermarket.member.dto.LoginResponseDto;
import dev.seo.navermarket.member.dto.SignupRequestDto;
import dev.seo.navermarket.member.dto.SignupResponseDto;
import dev.seo.navermarket.security.dto.TokenResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @file AuthRestController.java
 * @brief 인증(로그인 및 회원가입) 관련 REST API 요청을 처리하는 컨트롤러 클래스입니다.
 * 프론트엔드로부터의 HTTP 요청을 받아 인증 로직을 수행하고 응답합니다.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
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
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
    
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        log.info("Login attempt for userId: {}", loginRequestDto.getUserId());
        TokenResponseDto tokenResponse = authService.login(loginRequestDto);

        // 토큰을 HTTP 헤더에 담아 전송하는 것도 일반적입니다. 여기서는 바디에 포함하여 반환합니다.
        // HttpHeaders headers = new HttpHeaders();
        // headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + tokenResponse.getAccessToken());
        // headers.add("Refresh-Token", tokenResponse.getRefreshToken());

        return ResponseEntity.ok(tokenResponse);
    }

    /**
     * @brief 리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급합니다.
     * @param refreshToken 클라이언트가 요청 헤더에 담아 보낸 리프레시 토큰
     * @return 새로 발급된 액세스 토큰 및 리프레시 토큰을 포함하는 ResponseEntity
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refreshTokens(@RequestHeader("Refresh-Token") String refreshToken) {
        log.info("Token refresh request received.");
        TokenResponseDto tokenResponse = authService.refreshTokens(refreshToken);
        return ResponseEntity.ok(tokenResponse);
    }
    
    /**
     * @brief 현재 로그인된 사용자 정보를 JWT 토큰을 통해 조회하는 API 엔드포인트입니다.
     * GET /api/auth/me
     * @param authorizationHeader Authorization 헤더 (Bearer JWT_TOKEN)
     * @return ResponseEntity<LoginResponseDto> 사용자 정보 DTO와 HTTP 상태 코드
     */
    @GetMapping("/me")
    public ResponseEntity<LoginResponseDto> getMyInfo(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        log.info("사용자 정보 조회 요청 수신.");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("Authorization 헤더가 없거나 'Bearer '로 시작하지 않습니다.(토큰 부재)");
            // HTTP 401 Unauthorized 응답. body는 LoginResponseDto의 기본 값 또는 null
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(LoginResponseDto.builder().accessToken(null).userId("").build()); // 최소 필드만 채움
        }
        
        // "Bearer " 접두사 제거하여 순수 토큰 문자열 추출
        String accessToken = authorizationHeader.substring(7);
        
        // JWT 관련 로직을 AuthService로 위임
        // AuthService에서 InvalidTokenException, UserNotFoundException 등을 던질 것이므로
        // 여기서는 별도의 try-catch 없이 GlobalExceptionHandler에 위임합니다.
        LoginResponseDto userInfo = authService.getAuthenticatedUserInfo(accessToken);
        log.info("사용자 정보 반환: userId={}", userInfo.getUserId());
        return ResponseEntity.ok(userInfo);
    }

    /**
     * @brief 사용자를 로그아웃 처리하고, 서버에 저장된 리프레시 토큰을 무효화합니다.
     * POST /api/auth/logout
     * @param refreshToken 클라이언트가 요청 헤더에 담아 보낸 리프레시 토큰
     * @return ResponseEntity<Void> 로그아웃 성공 시 HTTP 상태 코드 (204 No Content)
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Refresh-Token") String refreshToken) {
        log.info("Logout request received.");
        authService.logout(refreshToken);
        return ResponseEntity.noContent().build(); // 204 No Content 반환
    }
    
}
