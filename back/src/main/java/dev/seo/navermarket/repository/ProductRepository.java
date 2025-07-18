package dev.seo.navermarket.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.seo.navermarket.product.domain.ProductEntity;
import dev.seo.navermarket.product.domain.ProductStatus;

import java.util.Optional;


/**
 * @file ProductRepository.java
 * @brief 'product' 테이블에 대한 데이터베이스 접근을 담당하는 JPA 레포지토리 인터페이스입니다.
 * JpaRepository를 상속받아 기본적인 CRUD 기능을 제공하며,
 * 추가적인 쿼리 메서드를 정의하여 특정 조건의 상품을 조회할 수 있습니다.
 */
@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long>{

	/**
     * @brief 다양한 조건으로 상품 목록을 조회하고 페이지네이션을 지원합니다.
     * DELETED 상태를 제외하며, 대표 이미지(imageOrder=0)를 함께 가져옵니다.
     *
     * @param status 상품 상태 필터링 (선택 사항)
     * @param categoryName 카테고리 필터링 (선택 사항)
     * @param memberId 판매자 ID 필터링 (선택 사항)
     * @param title 제목 검색 (부분 일치, 선택 사항)
     * @param pageable 페이지네이션 및 정렬 정보
     * @return 조건에 맞는 ProductEntity 페이지 (대표 이미지 포함)
     */
	@Query("SELECT p FROM ProductEntity p LEFT JOIN FETCH p.detailImages di " +
	           "WHERE (:status IS NULL OR p.status = :status) AND " +
	           "(:categoryName IS NULL OR p.category = :categoryName) AND " +
	           "(:memberId IS NULL OR p.memberId = :memberId) AND " +
	           "(:title IS NULL OR p.title LIKE %:title%) AND " +
	           "p.status <> 'DELETED' AND (di.imageOrder = 0 OR di.detailImageId IS NULL)"
	           )
	Page<ProductEntity> findProductsByCriteria(
            @Param("status") ProductStatus status,
            @Param("categoryName") String categoryName,
            @Param("memberId") Long memberId,
            @Param("title") String title,
            Pageable pageable
            );
			
	/**
     * @brief 메인/검색 페이지 기본 표시용: 상품 상태 우선순위 (ACTIVE/RESERVED 우선, SOLD_OUT 다음) 및 최신 등록일 기준으로 정렬된
     * 모든 상품 목록을 조회합니다. DELETED 상태는 제외되며, 대표 이미지(imageOrder=0)를 함께 가져옵니다.
     *
     * @param pageable 페이지네이션 정보 (정렬은 쿼리 내에서 정의)
     * @return 정렬된 ProductEntity 페이지 (대표 이미지 포함)
     */
	@Query("SELECT p FROM ProductEntity p LEFT JOIN FETCH p.detailImages di " +
	           "WHERE p.status <> 'DELETED' AND (di.imageOrder = 0 OR di.detailImageId IS NULL) ORDER BY " +
	           "CASE p.status " +
	           "WHEN 'ACTIVE' THEN 1 " +
	           "WHEN 'RESERVED' THEN 2 " +
	           "WHEN 'SOLD_OUT' THEN 3 " +
	           "ELSE 4 END, " +
	           "p.createdAt DESC"
	           )
	    Page<ProductEntity> findAllActiveAndOrdered(Pageable pageable);
	
	// 특정 상품 ID에 해당하는 모든 상세 이미지를 삭제하는 메서드
    void deleteByProduct_ProductId(Long productId);
    
}
