package dev.seo.navermarket.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @file SignupResponseDto.java
 * @brief 회원가입 성공 시 클라이언트에 반환되는 응답 DTO 클래스입니다.
 * 회원가입 성공 메시지와 함께 필요한 경우 새로 생성된 회원 정보를 포함할 수 있습니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupResponseDto {
	private Long memberId;
	private String userId;
	private String userName;
	private String message;
}
