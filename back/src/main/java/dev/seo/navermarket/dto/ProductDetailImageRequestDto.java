package dev.seo.navermarket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @file ProductDetailImageRequestDto.java
 * @brief 상품 상세 이미지 생성 및 업데이트 요청을 위한 DTO 클래스입니다.
 * 클라이언트로부터 이미지 URL과 순서 정보를 받습니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ProductDetailImageRequestDto {
	
	@NotBlank(message = "이미지 등록은 필수 입니다.")
	private String imageUrl;
}