package dev.seo.navermarket.dto;

import java.math.BigDecimal;
import java.util.List;

import dev.seo.navermarket.entity.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @file ProductUpdateRequestDto.java
 * @brief 기존 상품 정보 수정 요청 시 사용되는 DTO 클래스입니다.
 * 클라이언트로부터 수정할 상품 정보와 새로운 이미지 URL 목록을 받아 엔티티로 업데이트하는 데 사용됩니다.
 */
@Getter
@Setter
@ToString // Lombok: toString() 메서드를 자동으로 생성하여 객체 내용을 쉽게 출력할 수 있게 함
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateRequestDto {
	// 상품 ID는 PathVariable로 받으므로 DTO에 포함하지 않음
	private String title;
    private String category;
    private BigDecimal price;
    private String description;
	
	@Valid
	@Size(min = 1, message = "상품 이미지는 최소 1장 이상 등록해야 합니다.") // 이미지 변경 시 최소 1장 이상
	private List<ProductDetailImageRequestDto> detailImages;
	
	private ProductStatus status;
	private Integer views;
}
