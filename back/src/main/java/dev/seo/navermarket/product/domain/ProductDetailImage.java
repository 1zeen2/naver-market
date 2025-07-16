package dev.seo.navermarket.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @file ProductDetailImage.java
 * @brief 'product_detail_image' 테이블과 매핑되는 JPA 엔티티 클래스입니다.
 * 상품의 상세 이미지 정보를 담고 있으며, ProductEntity와 N:1 관계를 가집니다.
 */
@Entity
@Table(name = "product_detail_image")
@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProductDetailImage {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_image_id", nullable = false)
    private Long detailImageId;
	
	// ProductEntity와의 N:1 관계 설정
	// FetchType.LAZY: 지연 로딩을 설정하여 ProductDetailImage를 로드할 때 Product 엔티티를 즉시 로드하지 않도록 함.
	// @JoinColumn: 외래 키 컬럼을 지정합니다. (product_id)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private ProductEntity product;
	
	@Column(name = "image_url", nullable = false, length = 255)
	private String imageUrl;
	
	@Column(name = "image_order", nullable = false) // image_order는 0부터 시작하도록 강제 (대표 이미지 = image_order = 0)
	private Integer imageOrder;
	
}
