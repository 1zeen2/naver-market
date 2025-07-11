package dev.seo.navermarket.dto;

import dev.seo.navermarket.entity.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @file ProductListResponseDto.java
 * @brief 상품 목록 조회 시 사용되는 응답 DTO 클래스입니다.
 * 각 상품의 주요 정보와 대표 이미지를 포함합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductListResponseDto {
    private Long productId;
    private Long memberId;
    private String title;
    private String category;
    private BigDecimal price;
    private String mainImageUrl; // 상품 목록에서 대표 이미지를 보여주기 위한 필드
    private ProductStatus status;
    private Integer views;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}