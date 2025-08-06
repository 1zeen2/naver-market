package dev.seo.navermarket.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @file CheckResponseDto.java
 * @brief 아이디, 닉네임, 이메일, 쿠폰,  등 중복 확인 API의 표준 응답 DTO 클래스입니다.
 * @description
 * 중복 확인 결과(사용 가능 여부)와 해당 결과에 대한 메시지를 포함합니다.
 * 클라이언트의 CheckAvailabilityResponse 인터페이스와 일치합니다.
 */
@Getter
@Setter
@ToString
@Builder
public class CheckResponseDto {

	private boolean isAvailable;
    private String message;

}