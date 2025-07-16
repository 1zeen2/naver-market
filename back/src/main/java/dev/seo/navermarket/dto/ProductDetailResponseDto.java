package dev.seo.navermarket.dto;

import dev.seo.navermarket.entity.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @file ProductDetailResponseDto.java
 * @brief 상품 상세 조회 시 사용되는 응답 DTO 클래스입니다.
 * 상품의 모든 정보와 상세 이미지 URL 목록을 포함합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailResponseDto {
	private Long productId;
    private Long memberId;
    private String title;
    private String category;
    private BigDecimal price;
    private String description;
    private List<String> imageUrls; // 모든 상세 이미지 URL (image_order 순서대로 정렬됨)
    private ProductStatus status;
    private Integer views;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}