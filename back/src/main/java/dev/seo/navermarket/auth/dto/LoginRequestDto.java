package dev.seo.navermarket.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @file LoginRequestDto.java
 * @brief 사용자 로그인 요청 데이터를 담는 DTO입니다.
 * 사용자 ID와 비밀번호를 포함하며, 유효성 검사 어노테이션이 적용됩니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDto {

    @NotBlank(message = "사용자 ID는 필수 입력 항목입니다.")
    @Size(min = 4, max = 20, message = "사용자 ID는 4자 이상 20자 이하로 입력해주세요.")
    private String userId;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, max = 30, message = "비밀번호는 8자 이상 30자 이하로 입력해주세요.")
    private String userPwd;
    
}