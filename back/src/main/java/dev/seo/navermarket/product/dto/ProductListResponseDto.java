package dev.seo.navermarket.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import dev.seo.navermarket.product.domain.ProductEntity;
import dev.seo.navermarket.product.domain.ProductStatus;

/**
 * @file ProductListResponseDto.java
 * @brief 상품 목록 조회 시 사용되는 응답 DTO 클래스입니다.
 * 각 상품의 주요 정보와 대표 이미지를 포함합니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListResponseDto {
	private Long productId;
    private Long sellerId; // 판매자 고유 ID (PK)
    private String sellerUserId; // 판매자 로그인 ID
    private String title;
    private Long price;
    private String category;
    private String mainImageUrl;
    private String preferredTradeLocation;
    private ProductStatus status;
    private Integer views;
    private LocalDateTime createdAt;
    
    /**
     * @brief ProductEntity로부터 ProductListResponseDto를 생성하는 팩토리 메서드입니다.
     * @param productEntity 변환할 ProductEntity 객체
     * @return ProductListResponseDto 변환된 DTO 객체
     */
    public static ProductListResponseDto fromEntity(ProductEntity productEntity) {
    	Long sellerId = null;
        String sellerUserId = null;
        
        if (productEntity.getSeller() != null) {
            sellerId = productEntity.getSeller().getMemberId();
            sellerUserId = productEntity.getSeller().getUserId();
        } else {
            System.err.println("경고: ProductEntity " + productEntity.getProductId() + "의 판매자 정보가 null입니다. 데이터 무결성 확인이 필요합니다.");
        }

        return ProductListResponseDto.builder()
                .productId(productEntity.getProductId())
                .sellerId(sellerId) // 안전하게 설정
                .sellerUserId(sellerUserId) // 안전하게 설정
                .title(productEntity.getTitle())
                .price(productEntity.getPrice())
                .category(productEntity.getCategory())
                .mainImageUrl(productEntity.getMainImageUrl())
                .preferredTradeLocation(productEntity.getPreferredTradeLocation())
                .status(productEntity.getStatus())
                .views(productEntity.getViews())
                .createdAt(productEntity.getCreatedAt())
                .build();
    }
}