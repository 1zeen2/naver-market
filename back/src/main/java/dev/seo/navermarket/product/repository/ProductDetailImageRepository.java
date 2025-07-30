package dev.seo.navermarket.product.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.seo.navermarket.product.domain.ProductDetailImage;

/**
 * @file ProductDetailImageRepository.java
 * @brief 'product_detail_image' 테이블에 대한 JPA Repository 인터페이스입니다.
 * ProductDetailImage 엔티티의 데이터베이스 CRUD 작업을 담당합니다.
 */
@Repository
public interface ProductDetailImageRepository extends JpaRepository<ProductDetailImage, Long> {

	/**
     * @brief 특정 상품(ProductEntity)에 속한 모든 상세 이미지를 imageOrder 순서로 조회합니다.
     *
     * @param productId 이미지를 조회할 상품의 고유 ID
     * @return 해당 상품에 속한 ProductDetailImage 리스트
     */
	List<ProductDetailImage> findByProduct_ProductIdOrderByImageOrderAsc(Long productId);
	
	/**
     * @brief 특정 상품에 속한 모든 상세 이미지를 삭제합니다.
     * 이 메서드는 주로 상품 삭제 시 연관된 이미지를 일괄 삭제할 때 사용됩니다.
     *
     * @param productId 이미지를 삭제할 상품의 고유 ID
     */
	void deleteByProduct_ProductId(Long productId);
	
	// ProductEntity 객체를 직접 인자로 받아 삭제하는 메서드도 유용할 수 있습니다.
    // void deleteByProduct(ProductEntity product);
}