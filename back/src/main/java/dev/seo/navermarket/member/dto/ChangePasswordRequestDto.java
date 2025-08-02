package dev.seo.navermarket.member.dto;

import dev.seo.navermarket.validator.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @file ChangePasswordRequestDto.java
 * @brief 사용자 비밀번호 변경 요청 시 사용되는 DTO 클래스입니다.
 * 새로운 비밀번호와 비밀번호 확인 필드를 포함하며, 유효성 검사를 수행합니다.
 */
@Getter
@Setter
@ToString
public class ChangePasswordRequestDto {

	@NotBlank(message = "현재 비밀번호는 필수 입력 항목입니다.")
	private String currentPassword;
	
	@NotBlank(message = "새 비밀번호는 필수 입력 항목입니다.")
	@ValidPassword
	private String newPassword;
	
	@NotBlank(message = "새 비밀번호 확인은 필수 입력 항목입니다.")
	private String confirmNewPassword;
	
}