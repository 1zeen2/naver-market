package dev.seo.navermarket.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @file ErrorResponseDto.java
 * @brief API 오류 발생 시 클라이언트에 반환되는 표준 응답 DTO 클래스입니다.
 * 오류 메시지와 상태 코드를 포함합니다.
 */
@Getter
@Setter
@ToString
@Builder
public class ErrorResponseDto {
    private int status;
    private String message;
    private String code;
}