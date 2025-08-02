package dev.seo.navermarket.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import dev.seo.navermarket.product.domain.ProductDetailImage;
import dev.seo.navermarket.product.domain.ProductEntity;
import dev.seo.navermarket.product.domain.ProductStatus;

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
    private String sellerNickname;
    private String title;
    private String category;
    private Long price;
    private String description;
    private String mainImageUrl;
    private List<String> detailImageUrls;
    private String tradeAreaMain;
    private String tradeAreaSub;
    private String tradeAreaDetail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ProductStatus status;
    private Integer views;
    
    /**
     * @brief ProductEntity로부터 ProductDetailResponseDto를 생성하는 팩토리 메서드입니다.
     * @param productEntity 변환할 ProductEntity 객체
     * @return ProductDetailResponseDto 변환된 DTO 객체
     */
    public static ProductDetailResponseDto fromEntity(ProductEntity productEntity) {
    	return ProductDetailResponseDto.builder()
                .productId(productEntity.getProductId())
                .sellerId(productEntity.getSeller().getMemberId())
                .sellerNickname(productEntity.getSeller().getNickname())
                .title(productEntity.getTitle())
                .description(productEntity.getDescription())
                .price(productEntity.getPrice())
                .category(productEntity.getCategory())
                .mainImageUrl(productEntity.getMainImageUrl())
                .detailImageUrls(productEntity.getDetailImages().stream()
                        .sorted((img1, img2) -> img1.getImageOrder().compareTo(img2.getImageOrder()))
                        .map(ProductDetailImage::getImageUrl)
                        .collect(Collectors.toList()))
                .tradeAreaMain(productEntity.getTradeAreaMain())
                .tradeAreaSub(productEntity.getTradeAreaSub())
                .tradeAreaDetail(productEntity.getTradeAreaDetail())
                .status(productEntity.getStatus())
                .views(productEntity.getViews())
                .createdAt(productEntity.getCreatedAt())
                .updatedAt(productEntity.getUpdatedAt())
                .build();
    }
}