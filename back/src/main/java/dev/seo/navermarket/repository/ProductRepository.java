package dev.seo.navermarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import dev.seo.navermarket.entity.ProductStatus;
import dev.seo.navermarket.product.domain.ProductEntity;

import java.util.List;


/**
 * @file ProductRepository.java
 * @brief 'product' 테이블에 대한 데이터베이스 접근을 담당하는 JPA 레포지토리 인터페이스입니다.
 * JpaRepository를 상속받아 기본적인 CRUD 기능을 제공하며,
 * 추가적인 쿼리 메서드를 정의하여 특정 조건의 상품을 조회할 수 있습니다.
 */
@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long>{

	/**
     * @brief 특정 상태의 모든 상품을 최신 등록일 기준으로 내림차순 정렬하여 조회합니다.
     * 이 메서드는 특정 상태 (ACTIVE, RESERVED, SOLD_OUT 등) 필터링 버튼 클릭 시 사용됩니다.
     * @param status 조회할 상품 상태
     * @return 해당 상태의 상품 엔티티 리스트
     */
    List<ProductEntity> findByStatusOrderByCreatedAtDesc(ProductStatus status);

    /**
     * @brief 특정 카테고리에 속하며 특정 상태의 상품을 최신 등록일 기준으로 내림차순 정렬하여 조회합니다.
     * @param category 조회할 카테고리
     * @param status 상품 상태
     * @return 특정 카테고리의 해당 상태 상품 엔티티 리스트
     */
    List<ProductEntity> findByCategoryAndStatusOrderByCreatedAtDesc(String category, ProductStatus status);

    /**
     * @brief 특정 판매자의 특정 상태의 모든 상품을 최신 등록일 기준으로 내림차순 정렬하여 조회합니다.
     * @param memberId 판매자 아이디 번호 (member 테이블의 member_id)
     * @param status 상품 상태
     * @return 특정 판매자의 해당 상태 상품 엔티티 리스트
     */
    List<ProductEntity> findByMemberIdAndStatusOrderByCreatedAtDesc(Long memberId, ProductStatus status);

    /**
     * @brief 특정 제목이 포함되어있으며 특정 상태의 모든 상품을 최신 등록일 기준으로 내림차순하여 조회합니다.
     * @param title 제목 (부분 일치 검색)
     * @param status 상품 상태
     * @return 특정 제목이 포함된 상품 엔티티 리스트
     */
    List<ProductEntity> findByTitleContainingAndStatusOrderByCreatedAtDesc(String title, ProductStatus status);

    /**
     * @brief 메인 페이지 기본 표시용: DELETED 상태를 제외한 모든 상품을 최신 등록일 기준으로 내림차순 정렬하여 조회합니다.
     * @return DELETED를 제외한 모든 상품 엔티티 리스트 (최신순)
     */
    List<ProductEntity> findByStatusIsNotOrderByCreatedAtDesc(ProductStatus status);
    
    /**
     * @brief 검색/카테고리 페이지 기본 표시용: 상품 상태를 ACTIVE/RESERVED 우선 (동일 우선순위), 그 다음 SOLD_OUT 순으로 정렬하고,
     * 각 그룹 내에서는 최신 등록일 기준으로 내림차순 정렬하여 모든 상품을 조회합니다. DELETED 상태는 제외됩니다.
     * @return 정렬된 상품 엔티티 리스트
     */
    @Query("SELECT p FROM ProductEntity p " +
    	   "WHERE p.status IN ('ACTIVE', 'RESERVED', 'SOLD_OUT') " +
    	   "ORDER BY CASE p.status " +
    	   "	WHEN 'ACTIVE' THEN 1 " +
    	   "	WHEN 'RESERVED' THEN 1 " +
    	   "	WHEN 'SOLD_OUT' THEN 2 " +
    	   "	ELSE 3 " +
    	   "END, p.createdAt DESC"
    	   )
    List<ProductEntity> findAllProductsOrderedByStatusPriorityAndCreatedAtDesc();
}
