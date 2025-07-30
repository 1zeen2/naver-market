package dev.seo.navermarket.member.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @file LoginResponseDto.java
 * @brief 사용자 로그인 성공 시 반환되는 DTO (Data Transfer Object) 클래스입니다.
 * 인증 토큰, 사용자 ID, 사용자 이름 등 프론트엔드에 필요한 정보를 담습니다.
 */
@Getter
@Setter
@ToString
@Builder // Lombok: 빌더 패턴을 자동으로 생성하여 객체 생성을 용이하게 함.
public class LoginResponseDto {
	private String token;
	private Long memberId;
	private String userId;
	private String userName;
}