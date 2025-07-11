package dev.seo.navermarket.service;

import dev.seo.navermarket.dto.ProductCreateRequestDto;
import dev.seo.navermarket.dto.ProductDetailResponseDto;
import dev.seo.navermarket.dto.ProductListResponseDto;
import dev.seo.navermarket.dto.ProductUpdateRequestDto;
import dev.seo.navermarket.entity.ProductStatus;
import dev.seo.navermarket.product.domain.ProductEntity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

/**
 * @file ProductService.java
 * @brief 상품 관련 비즈니스 로직을 정의하는 서비스 인터페이스입니다.
 * 이 인터페이스는 상품 조회, 생성, 수정, 삭제 등의 기능을 추상화합니다.
 * 모든 조회 메서드는 DTO를 반환하며, 페이지네이션 및 다양한 필터링을 지원합니다.
 */
public interface ProductService {

	/**
     * @brief 다양한 조건(페이지, 크기, 정렬, 상태, 카테고리, 판매자 ID, 제목)으로 상품 목록을 조회하고 페이지네이션을 지원합니다.
     * 이 메서드는 메인 페이지, 검색 페이지, 카테고리 페이지 등 다양한 목록 조회 시나리오에 사용됩니다.
     *
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지당 아이템 수
     * @param sortBy 정렬 기준 필드 (예: createdAt, price, views)
     * @param direction 정렬 방향 (asc 또는 desc)
     * @param status 상품 상태 필터링 (ACTIVE, RESERVED, SOLD_OUT, DELETED 등, 선택 사항)
     * @param categoryName 카테고리 필터링 (선택 사항)
     * @param memberId 판매자 ID 필터링 (선택 사항)
     * @param title 제목 검색 (부분 일치, 선택 사항)
     * @param defaultOrder 메인/검색 페이지 기본 정렬 적용 여부 (true일 경우 특정 정렬 우선순위 적용)
     * @return 조건에 맞는 ProductListResponseDto 페이지
     * @throws IllegalArgumentException 유효하지 않은 상품 상태가 제공될 경우
     */
	Page<ProductListResponseDto> getProducts(
            int page, int size, String sortBy, String direction,
            String status, String categoryName, Long memberId, String title,
            boolean defaultOrder);
	
	/**
     * @brief 특정 상품 ID로 상품 상세 정보를 조회합니다.
     *
     * @param productId 조회할 상품의 고유 ID
     * @return 조회된 ProductDetailResponseDto (Optional로 감싸져 반환), 상품을 찾을 수 없으면 Optional.empty()
     */
	Optional<ProductDetailResponseDto> getProductDetail(Long productId);
	
	/**
	 * 특정 상품의 조회수를 1 증가시킵니다.
	 * @param productId 조회수를 증가시킬 상품의 고유 ID
	 */
	void incrementViewCount(Long productId);
	
	/**
     * @brief 새로운 상품을 등록합니다.
     * 상품의 기본 정보와 함께 상세 이미지 목록을 받아 처리합니다.
     *
     * @param productDto 등록할 상품 정보와 이미지 URL 목록이 담긴 DTO (ProductCreateRequestDto)
     * @return 등록된 상품의 요약 정보 (ProductListResponseDto)
     */
    ProductListResponseDto createProduct(ProductCreateRequestDto productDto);
    
    /**
     * @brief 기존 상품 정보를 수정합니다.
     * 상품의 고유 ID를 기반으로 상품 정보와 이미지 목록을 업데이트합니다.
     *
     * @param productId 수정할 상품의 고유 ID
     * @param productDto 수정할 상품 정보와 새로운 이미지 URL 목록이 담긴 DTO (ProductUpdateRequestDto)
     * @return 수정된 상품의 요약 정보 (ProductListResponseDto)
     * @throws RuntimeException 해당 ID의 상품을 찾을 수 없을 경우
     */
    ProductListResponseDto updateProduct(Long productId, ProductUpdateRequestDto productDto);
    
    /**
     * @brief 특정 상품을 삭제(소프트 삭제)합니다.
     * 실제 데이터 삭제가 아닌, 상품 상태를 DELETED로 변경합니다.
     * @param productId 삭제할 상품의 고유 ID
     * @throws RuntimeException 해당 ID의 상품을 찾을 수 없을 경우
     */
    void deleteProduct(Long productId);
}