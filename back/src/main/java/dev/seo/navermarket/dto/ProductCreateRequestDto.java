package dev.seo.navermarket.dto;

import java.math.BigDecimal;
import java.util.List;

import dev.seo.navermarket.entity.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @file ProductRequestDto.java
 * @brief 새로운 상품 등록 요청 시 사용되는 DTO (Data Transfer Object) 클래스입니다.
 * 프론트엔드에서 상품 등록 API로 전송되는 데이터를 담습니다.
 * 상품의 기본 정보와 함께 여러 장의 상세 이미지 정보를 포함합니다.
 */
@Getter
@Setter
@ToString // Lombok: toString() 메서드를 자동으로 생성하여 객체 내용을 쉽게 출력할 수 있게 해줌
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateRequestDto {
	private Long memberId;
	
	@NotBlank(message = "상품 제목은 필수입니다.")
    private String title;
	
	@NotBlank(message = "카테고리는 필수입니다.")
    private String category;
	
	@NotNull(message = "가격은 필수입니다.")
	@Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private BigDecimal price;
	
	@NotBlank(message = "상품 설명은 필수입니다.")
    private String description;
	
	private ProductStatus status;
    private Integer views;
	
	@Valid
    @NotNull(message = "상품 이미지는 최소 1장 이상 필수입니다.")
    @Size(min = 1, message = "상품 이미지는 최소 1장 이상 등록해야 합니다.")
    private List<ProductDetailImageRequestDto> detailImages;
	
    // views, status, created_at, updated_at은 백엔드에서 자동 설정되므로 DTO에 포함하지 않음.
}