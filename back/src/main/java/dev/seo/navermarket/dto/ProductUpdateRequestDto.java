package dev.seo.navermarket.dto;

import java.math.BigDecimal;

import dev.seo.navermarket.entity.ProductStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @file ProductUpdateRequestDto.java
 * @brief 상품 수정 요청 시 사용되는 DTO (Data Transfer Object) 클래스입니다.
 * 프론트엔드에서 상품 수정 API로 전송되는 데이터를 담습니다.
 */
@Getter
@Setter
@ToString // Lombok: toString() 메서드를 자동으로 생성하여 객체 내용을 쉽게 출력할 수 있게 함
public class ProductUpdateRequestDto {
	// 상품 ID는 PathVariable로 받으므로 DTO에 포함하지 않음
	private String title;
	private String category;
	private BigDecimal price;
	private String description;
	private String imageUrl;
	private ProductStatus status;
}
