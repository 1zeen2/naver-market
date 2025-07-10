package dev.seo.navermarket.service;

import dev.seo.navermarket.dto.ProductRequestDto;
import dev.seo.navermarket.dto.ProductUpdateRequestDto;
import dev.seo.navermarket.entity.ProductStatus;
import dev.seo.navermarket.product.domain.ProductEntity;

import java.util.List;

/**
 * @file ProductService.java
 * @brief 상품 관련 비즈니스 로직을 정의하는 서비스 인터페이스입니다.
 * 각 메서드는 상품 조회, 등록, 수정, 삭제 기능을 제공하며, 실제 구현은 ProductServiceImpl 클래스에서 구현
 */
public interface ProductService {

    // --- 조회 (Read) 메서드 ---

    /**
     * @brief 메인 페이지 기본 표시용: DELETED 상태를 제외한 모든 상품을 최신 등록일 기준으로 조회합니다.
     * @return DELETED를 제외한 모든 상품 엔티티 리스트 (최신순)
     */
    List<ProductEntity> getAllProductsForMainPage();

    /**
     * @brief 특정 상태의 모든 상품을 최신 등록일 기준으로 조회합니다.
     * 이 메서드는 특정 상태 필터링 버튼 클릭 시 사용됩니다.
     * @param status 조회할 상품 상태
     * @return 해당 상태의 상품 엔티티 리스트
     */
    List<ProductEntity> getProductsByStatus(ProductStatus status);

    /**
     * @brief 특정 카테고리에 속하며 특정 상태의 상품을 최신 등록일 기준으로 내림차순 정렬하여 조회합니다.
     * @param category 조회할 카테고리
     * @param status 상품 상태
     * @return 특정 카테고리의 해당 상태 상품 엔티티 리스트
     */
    List<ProductEntity> getProductsByCategoryAndStatus(String category, ProductStatus status);

    /**
     * @brief 특정 판매자의 특정 상태의 모든 상품을 최신 등록일 기준으로 내림차순 정렬하여 조회합니다.
     * @param memberId 판매자 아이디 번호
     * @param status 상품 상태
     * @return 특정 판매자의 해당 상태 상품 엔티티 리스트
     */
    List<ProductEntity> getProductsByMemberIdAndStatus(Long memberId, ProductStatus status);

    /**
     * @brief 특정 제목이 포함되어있으며 특정 상태의 모든 상품을 최신 등록일 기준으로 내림차순하여 조회합니다.
     * @param title 제목 (부분 일치 검색)
     * @param status 상품 상태
     * @return 특정 제목이 포함된 상품 엔티티 리스트
     */
    List<ProductEntity> getProductsByTitleContainingAndStatus(String title, ProductStatus status);

    /**
     * @brief 검색/카테고리 페이지 기본 표시용: 상품 상태 우선순위 (ACTIVE/RESERVED -> SOLD_OUT) 및 최신 등록일 기준으로 정렬된
     * 모든 상품 목록을 조회합니다. DELETED 상태는 제외됩니다.
     * @return 정렬된 상품 엔티티 리스트
     */
    List<ProductEntity> getAllProductsForSearchAndCategoryPage();

    /**
     * @brief 특정 상품 ID로 상품 정보를 조회합니다.
     * @param productId 조회할 상품의 고유 ID
     * @return 조회된 ProductEntity (Optional로 감싸져 반환)
     */
    ProductEntity getProductById(Long productId); // 단일 상품 조회 추가

    // --- CRUD (Create, Update, Delete) 메서드 ---

    /**
     * @brief 새로운 상품을 등록합니다.
     * @param productDto 등록할 상품 정보가 담긴 DTO
     * @return 등록된 ProductEntity 객체
     */
    ProductEntity createProduct(ProductRequestDto productDto);

    /**
     * @brief 기존 상품 정보를 수정합니다.
     * @param productId 수정할 상품의 고유 ID
     * @param productDto 수정할 상품 정보가 담긴 DTO
     * @return 수정된 ProductEntity 객체
     */
    ProductEntity updateProduct(Long productId, ProductUpdateRequestDto productDto);

    /**
     * @brief 특정 상품을 삭제(소프트 삭제)합니다.
     * 실제 데이터 삭제가 아닌, 상태를 DELETED로 변경합니다.
     * @param productId 삭제할 상품의 고유 ID
     */
    void deleteProduct(Long productId);
}