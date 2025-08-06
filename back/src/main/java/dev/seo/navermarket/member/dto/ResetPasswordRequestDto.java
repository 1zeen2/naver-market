// src/main/java/dev/seo/navermarket/member/dto/ResetPasswordRequestDto.java
package dev.seo.navermarket.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief 비밀번호 재설정 요청 시 사용되는 DTO 클래스입니다.
 * 재설정 토큰과 새로운 비밀번호 정보를 포함합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequestDto {
	
    private String resetToken;
    private String newPassword;
    private String confirmNewPassword;
    
}
