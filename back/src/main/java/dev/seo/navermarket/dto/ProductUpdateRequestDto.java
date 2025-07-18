package dev.seo.navermarket.dto;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @file ProductUpdateRequestDto.java
 * @brief 상품 정보 수정 요청 시 사용되는 DTO입니다.
 * 프론트엔드에서 상품 수정 API로 전송되는 데이터를 담습니다.
 * 모든 필드가 필수는 아니며, 변경하고자 하는 필드만 포함될 수 있습니다.
 * 이미지 파일은 새로운 파일로 대체될 경우에만 전송됩니다.
 */
@Getter
@ToString // Lombok: toString() 메서드를 자동으로 생성하여 객체 내용을 쉽게 출력할 수 있게 함
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateRequestDto {
	// 상품 ID는 PathVariable로 받으므로 DTO에 포함하지 않음
	private String title;
    private String category;
    
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private BigDecimal price;
    
    private String description;
    
    private MultipartFile mainImage;
    
    private String preferredTradeLocation;
	
	@Valid
	@Size(min = 1, message = "상품 이미지는 최소 1장 이상 등록해야 합니다.") // 이미지 변경 시 최소 1장 이상
	private List<MultipartFile> detailImages;
	
//	private ProductStatus status;
//	private Integer views;
}
