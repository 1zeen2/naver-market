package dev.seo.navermarket.dto;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @file ProductRequestDto.java
 * @brief 새로운 상품 등록 요청 시 사용되는 DTO (Data Transfer Object) 클래스입니다.
 * 프론트엔드에서 상품 등록 API로 전송되는 데이터를 담습니다.
 * 상품의 기본 정보와 함께 여러 장의 상세 이미지 정보를 포함합니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString // Lombok: toString() 메서드를 자동으로 생성하여 객체 내용을 쉽게 출력할 수 있게 해줌
public class ProductCreateRequestDto {
	
	// memberId는 JWT에서 추출하여 서비스 계층에서 주입. DTO에 포함하지 않음
	
	@NotBlank(message = "상품 제목은 필수입니다.")
    private String title;
	
	@NotBlank(message = "카테고리는 필수입니다.")
    private String category;
	
	@NotNull(message = "가격은 필수입니다.")
	@Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private BigDecimal price;
	
	@NotBlank(message = "상품 설명은 필수입니다.")
    private String description;
	
	@NotNull(message = "대표 사진은 필수 입니다.")
	private MultipartFile mainImage;
	
	// 거래 희망 지역 필드
	@NotBlank(message = "거래 희망 지역은 필수 입니다.")
	private String preferredTradeLocation;
	
    @Size(max = 9, message = "상품 이미지는 최대 9장까지 등록 가능합니다.")
    private List<MultipartFile> detailImages;
}