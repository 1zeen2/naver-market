package dev.seo.navermarket.product.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @file ProductUpdateRequestDto.java
 * @brief 상품 정보 수정 요청 시 사용되는 DTO입니다.
 * 프론트엔드에서 상품 수정 API로 전송되는 텍스트 데이터를 담습니다.
 * 모든 필드가 필수는 아니며, 변경하고자 하는 필드만 포함될 수 있습니다.
 */
@ToString // Lombok: toString() 메서드를 자동으로 생성하여 객체 내용을 쉽게 출력할 수 있게 함
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ProductUpdateRequestDto {
	// 상품 ID는 PathVariable로 받으므로 DTO에 포함하지 않음
	private String title;
    private String category;
    
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private Long price;
    
    private String description;
    
    private String preferredTradeLocation;
	
}
