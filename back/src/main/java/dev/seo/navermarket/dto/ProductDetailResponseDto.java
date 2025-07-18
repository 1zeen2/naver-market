package dev.seo.navermarket.dto;

import dev.seo.navermarket.product.domain.ProductDetailImage;
import dev.seo.navermarket.product.domain.ProductEntity;
import dev.seo.navermarket.product.domain.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @file ProductDetailResponseDto.java
 * @brief 상품 상세 조회 시 사용되는 응답 DTO 클래스입니다.
 * 상품의 모든 정보와 상세 이미지 URL 목록을 포함합니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailResponseDto {
	private Long productId;
    private Long sellerId; // 판매자 ID
    private String sellerUserId; // 판매자 이름
    private String title;
    private String description;
    private BigDecimal price;
    private String category;
    private String mainImageUrl;
    private List<String> detailImageUrls;
    private String preferredTradeLocation;
    private ProductStatus status;
    private Integer views;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * @brief ProductEntity로부터 ProductDetailResponseDto를 생성하는 팩토리 메서드입니다.
     * @param productEntity 변환할 ProductEntity 객체
     * @return ProductDetailResponseDto 변환된 DTO 객체
     */
    public static ProductDetailResponseDto fromEntity(ProductEntity productEntity) {
    	return ProductDetailResponseDto.builder()
                .productId(productEntity.getProductId())
                .sellerId(productEntity.getSeller().getMemberId())
                .sellerUserId(productEntity.getSeller().getUserId()) // product.getSeller().getUserId() 사용
                .title(productEntity.getTitle())
                .description(productEntity.getDescription())
                .price(productEntity.getPrice())
                .category(productEntity.getCategory())
                .mainImageUrl(productEntity.getMainImageUrl())
                .detailImageUrls(productEntity.getDetailImages().stream()
                        .sorted((img1, img2) -> img1.getImageOrder().compareTo(img2.getImageOrder()))
                        .map(ProductDetailImage::getImageUrl)
                        .collect(Collectors.toList()))
                .preferredTradeLocation(productEntity.getPreferredTradeLocation())
                .status(productEntity.getStatus())
                .views(productEntity.getViews())
                .createdAt(productEntity.getCreatedAt())
                .updatedAt(productEntity.getUpdatedAt())
                .build();
    }
}