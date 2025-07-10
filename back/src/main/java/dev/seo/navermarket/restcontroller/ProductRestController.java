package dev.seo.navermarket.restcontroller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.seo.navermarket.dto.ProductRequestDto;
import dev.seo.navermarket.dto.ProductUpdateRequestDto;
import dev.seo.navermarket.entity.ProductStatus;
import dev.seo.navermarket.product.domain.ProductEntity;
import dev.seo.navermarket.service.ProductService;
import lombok.RequiredArgsConstructor;

/**
 * @file ProductController.java
 * @brief 상품 관련 REST API 요청을 처리하는 컨트롤러 클래스입니다.
 * 프론트엔드로부터의 HTTP 요청을 받아 상품 데이터를 조회하고 응답합니다.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductRestController {
	
	private final ProductService productService;
	
	// --- 조회 (Read) EndPoint ---
	
	/**
     * @brief 메인 페이지 기본 표시용: DELETED 상태를 제외한 모든 상품을 최신 등록일 기준으로 조회합니다.
     * GET /api/products/main-feed
     * @return ResponseEntity<List<ProductEntity>> 상품 엔티티 리스트와 HTTP 상태 코드 (200 OK)
     */
	@GetMapping("/main-feed")
	public ResponseEntity<List<ProductEntity>> getMainFeedProducts() {
		List<ProductEntity> products = productService.getAllProductsForMainPage();
		return ResponseEntity.ok(products);
	}
	
	/**
     * @brief 특정 상태의 상품 목록을 조회하는 API 엔드포인트입니다.
     * 이 엔드포인트는 프론트엔드의 상태 필터링 버튼 클릭 시 사용됩니다.
     * GET /api/products/status?name={statusName}
     * @param statusName 조회할 상품 상태 이름 (ACTIVE, RESERVED, SOLD_OUT, DELETED)
     * @return ResponseEntity<List<ProductEntity>> 해당 상태의 상품 엔티티 리스트와 HTTP 상태 코드 (200 OK)
     */
	@GetMapping("/status")
	public ResponseEntity<List<ProductEntity>> getProductsByStatus(@RequestParam("name") ProductStatus statusName) {
		List<ProductEntity> products = productService.getProductsByStatus(statusName);
		return ResponseEntity.ok(products);
	}

	/**
     * @brief 특정 카테고리에 속하며 특정 상태의 상품 목록을 조회하는 API 엔드포인트입니다.
     * GET /api/products/category?name={categoryName}&status={statusName}
     * @param categoryName 조회할 카테고리 이름
     * @param statusName 조회할 상품 상태 이름
     * @return ResponseEntity<List<ProductEntity>> 특정 카테고리의 해당 상태 상품 엔티티 리스트와 HTTP 상태 코드 (200 OK)
     */
	@GetMapping("/category")
	public ResponseEntity<List<ProductEntity>> getProuctsByCategory(
			@RequestParam("name") String categoryName, @RequestParam("status") ProductStatus statusName) {
		List<ProductEntity> products = productService.getProductsByCategoryAndStatus(categoryName, statusName);
        return ResponseEntity.ok(products);
    }
	
	/**
     * @brief 특정 판매자의 특정 상태의 상품 목록을 조회하는 API 엔드포인트입니다.
     * GET /api/products/seller?memberId={memberId}&status={statusName}
     * @param memberId 판매자 회원 ID
     * @param statusName 조회할 상품 상태 이름
     * @return ResponseEntity<List<ProductEntity>> 특정 판매자의 해당 상태 상품 엔티티 리스트와 HTTP 상태 코드 (200 OK)
     */
	@GetMapping("/seller")
	public ResponseEntity<List<ProductEntity>> getProductsBySeller(
            @RequestParam Long memberId, @RequestParam ProductStatus statusName) {
		List<ProductEntity> products = productService.getProductsByMemberIdAndStatus(memberId, statusName);
        return ResponseEntity.ok(products);
	}
	
	/**
     * @brief 특정 제목이 포함되어있으며 특정 상태의 상품 목록을 조회하는 API 엔드포인트입니다.
     * GET /api/products/search?title={title}&status={statusName}
     * @param title 검색할 제목 (부분 일치)
     * @param statusName 조회할 상품 상태 이름
     * @return ResponseEntity<List<ProductEntity>> 특정 제목이 포함된 상품 엔티티 리스트와 HTTP 상태 코드 (200 OK)
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProductEntity>> searchProductsByTitle(
            @RequestParam String title, @RequestParam ProductStatus statusName) {
        List<ProductEntity> products = productService.getProductsByTitleContainingAndStatus(title, statusName);
        return ResponseEntity.ok(products);
    }
    
    /**
     * @brief 검색/카테고리 페이지 기본 표시용: 상품 상태 우선순위 (ACTIVE/RESERVED -> SOLD_OUT) 및 최신 등록일 기준으로 정렬된
     * 모든 상품 목록을 조회합니다. DELETED 상태는 제외됩니다.
     * GET /api/products/search-default-ordered
     * @return ResponseEntity<List<ProductEntity>> 정렬된 상품 엔티티 리스트와 HTTP 상태 코드 (200 OK)
     */
    @GetMapping("/search-default-ordered")
    public ResponseEntity<List<ProductEntity>> getSearchDefaultOrderedProducts() {
        List<ProductEntity> products = productService.getAllProductsForSearchAndCategoryPage();
        return ResponseEntity.ok(products);
    }

    /**
     * @brief 특정 상품 ID로 상품 상세 정보를 조회하는 API 엔드포인트입니다.
     * GET /api/products/{productId}
     * @param productId 조회할 상품의 고유 ID
     * @return ResponseEntity<ProductEntity> 조회된 상품 엔티티와 HTTP 상태 코드 (200 OK 또는 404 Not Found)
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductEntity> getProductDetail(@PathVariable Long productId) {
        try {
            ProductEntity product = productService.getProductById(productId);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            // 상품을 찾을 수 없을 경우 404 Not Found 반환
            return ResponseEntity.notFound().build();
        }
    }
    
    // --- CRUD (Create, Update, Delete) 엔드포인트 ---
    
    /**
     * @brief 새로운 상품을 등록하는 API 엔드포인트입니다.
     * POST /api/products
     * @param productDto 등록할 상품 정보가 담긴 DTO
     * @return ResponseEntity<ProductEntity> 등록된 상품 엔티티와 HTTP 상태 코드 (201 Created)
     */
    @PostMapping
    public ResponseEntity<ProductEntity> createProduct(@RequestBody ProductRequestDto productDto) {
    	ProductEntity createdProduct = productService.createProduct(productDto);
    	// 201 Created 상태 코드와 함께 등록된 상품 정보 반환
    	return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }
    
    /**
     * @brief 기존 상품 정보를 수정하는 API 엔드포인트입니다.
     * PUT /api/products/{productId}
     * @param productId 수정할 상품의 고유 ID (경로 변수)
     * @param productDto 수정할 상품 정보가 담긴 DTO (요청 본문)
     * @return ResponseEntity<ProductEntity> 수정된 상품 엔티티와 HTTP 상태 코드 (200 OK 또는 404 Not Found)
     */
    @PutMapping("/{productId}")
    public ResponseEntity<ProductEntity> updateProduct(
    		@PathVariable Long productId, @RequestBody ProductUpdateRequestDto productDto) {
        try {
        	ProductEntity updatedProduct = productService.updateProduct(productId, productDto);
            return ResponseEntity.ok(updatedProduct);
    	} catch (RuntimeException e) {
    		return ResponseEntity.notFound().build();
    	}
    }
    
    /**
     * @brief 특정 상품을 삭제(소프트 삭제)하는 API 엔드포인트입니다.
     * DELETE /api/products/{productId}
     * @param productId 삭제할 상품의 고유 ID (경로 변수)
     * @return ResponseEntity<Void> HTTP 상태 코드 (204 No Content 또는 404 Not Found)
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
    	try {
    		productService.deleteProduct(productId);
    		return ResponseEntity.noContent().build(); // 204 No Content 상태 코드 반환 (성공적으로 처리되었으나 반환할 본문이 없음)
    	} catch (RuntimeException e) {
    		return ResponseEntity.notFound().build();
    	}
    }
}
