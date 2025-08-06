package dev.seo.navermarket.auth.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * @brief 인증(로그인, 회원가입) 성공 후 클라이언트에게 반환할 응답 데이터를 위한 DTO입니다.
 * JWT 액세스 토큰과 사용자 ID, 그리고 응답 메시지를 포함합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDto {
	
    private String accessToken;
    private String userId;
    private String message;
    
}
