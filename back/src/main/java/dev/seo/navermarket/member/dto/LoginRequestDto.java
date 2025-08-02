package dev.seo.navermarket.member.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @file LoginRequestDto.java
 * @brief 사용자 로그인 요청 시 사용되는 DTO (Data Transfer Object) 클래스입니다.
 * 프론트엔드에서 로그인 API로 전송되는 사용자 ID와 비밀번호를 담습니다.
 */
@Getter
@Setter
@ToString
public class LoginRequestDto {
	
	private String userId;
	private String userPwd;
	
}