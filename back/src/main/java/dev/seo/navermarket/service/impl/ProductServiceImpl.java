package dev.seo.navermarket.service.impl;

import dev.seo.navermarket.dto.ProductRequestDto;
import dev.seo.navermarket.dto.ProductUpdateRequestDto;
import dev.seo.navermarket.entity.ProductStatus;
import dev.seo.navermarket.product.domain.ProductEntity;
import dev.seo.navermarket.repository.ProductRepository;
import dev.seo.navermarket.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @file ProductServiceImpl.java
 * @brief ProductService 인터페이스의 구현체 클래스입니다.
 * 상품 관련 비즈니스 로직의 실제 구현을 담당하며, ProductRepository를 통해 데이터베이스와 상호작용합니다.
 */
@Service
@RequiredArgsConstructor // Lombok: final 필드에 대한 생성자를 자동으로 생성하여 의존성 주입을 용이하게 합니다.
@Transactional(readOnly = true) // 클래스 레벨에서 모든 메서드에 읽기 전용 트랜잭션을 적용합니다.
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    // --- 조회 (Read) 메서드 구현 ---

    @Override
    public List<ProductEntity> getAllProductsForMainPage() {
        return productRepository.findByStatusIsNotOrderByCreatedAtDesc(ProductStatus.DELETED);
    }

    @Override
    public List<ProductEntity> getProductsByStatus(ProductStatus status) {
        return productRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Override
    public List<ProductEntity> getProductsByCategoryAndStatus(String category, ProductStatus status) {
        return productRepository.findByCategoryAndStatusOrderByCreatedAtDesc(category, status);
    }

    @Override
    public List<ProductEntity> getProductsByMemberIdAndStatus(Long memberId, ProductStatus status) {
        return productRepository.findByMemberIdAndStatusOrderByCreatedAtDesc(memberId, status);
    }

    @Override
    public List<ProductEntity> getProductsByTitleContainingAndStatus(String title, ProductStatus status) {
        return productRepository.findByTitleContainingAndStatusOrderByCreatedAtDesc(title, status);
    }

    @Override
    public List<ProductEntity> getAllProductsForSearchAndCategoryPage() {
        return productRepository.findAllProductsOrderedByStatusPriorityAndCreatedAtDesc();
    }

    /**
     * @brief 특정 상품 ID로 상품 정보를 조회합니다.
     * 상품이 존재하지 않을 경우 RuntimeException을 발생시킵니다.
     * @param productId 조회할 상품의 고유 ID
     * @return 조회된 ProductEntity
     * @throws RuntimeException 상품을 찾을 수 없을 경우
     */
    @Override
    public ProductEntity getProductById(Long productId) {
        // findById는 Optional을 반환하므로, orElseThrow를 사용하여 값이 없으면 예외를 발생시킵니다.
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다. (ID: " + productId + ")"));
    }

    // --- CRUD (Create, Update, Delete) 메서드 구현 ---

    /**
     * @brief 새로운 상품을 등록합니다.
     * @param productDto 등록할 상품 정보가 담긴 DTO
     * @return 등록된 ProductEntity 객체
     */
    @Override
    @Transactional // 쓰기 작업이므로 readOnly = true를 오버라이드하여 트랜잭션 활성화
    public ProductEntity createProduct(ProductRequestDto productDto) {
        // DTO에서 엔티티로 변환 시 ProductEntity.builder() 사용
        ProductEntity product = ProductEntity.builder()
                .memberId(productDto.getMemberId())
                .title(productDto.getTitle())
                .category(productDto.getCategory())
                .price(productDto.getPrice())
                .description(productDto.getDescription())
                .imageUrl(productDto.getImageUrl())
                .status(ProductStatus.ACTIVE) // 새 상품은 기본적으로 ACTIVE 상태
                .views(0) // 새 상품의 조회수는 0
                .build(); // 빌더 패턴의 마지막 호출

        // Repository를 사용하여 DB에 저장
        return productRepository.save(product);
    }

    /**
     * @brief 기존 상품 정보를 수정합니다.
     * @param productId 수정할 상품의 고유 ID
     * @param productDto 수정할 상품 정보가 담긴 DTO
     * @return 수정된 ProductEntity 객체
     * @throws RuntimeException 상품을 찾을 수 없을 경우
     */
    @Override
    @Transactional // 쓰기 작업이므로 readOnly = true를 오버라이드하여 트랜잭션 활성화
    public ProductEntity updateProduct(Long productId, ProductUpdateRequestDto productDto) {
        // 수정할 상품 엔티티를 조회합니다. 없으면 예외 발생.
        ProductEntity existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("수정할 상품을 찾을 수 없습니다. (ID: " + productId + ")"));

        // DTO의 값으로 엔티티를 업데이트합니다.
        // 각 필드가 null이 아닌 경우에만 업데이트.
        if (productDto.getTitle() != null) {
            existingProduct.setTitle(productDto.getTitle());
        }
        if (productDto.getCategory() != null) {
            existingProduct.setCategory(productDto.getCategory());
        }
        if (productDto.getPrice() != null) {
            existingProduct.setPrice(productDto.getPrice());
        }
        if (productDto.getDescription() != null) {
            existingProduct.setDescription(productDto.getDescription());
        }
        if (productDto.getImageUrl() != null) {
            existingProduct.setImageUrl(productDto.getImageUrl());
        }
        if (productDto.getStatus() != null) {
            existingProduct.setStatus(productDto.getStatus());
        }

        // 변경된 엔티티를 저장합니다. (JPA 영속성 컨텍스트 덕분에 save()를 명시적으로 호출하지 않아도 트랜잭션 커밋 시 변경 감지되어 업데이트될 수 있지만, 명시적으로 호출하는 것도 일반적입니다.)
        return productRepository.save(existingProduct);
    }

    /**
     * @brief 특정 상품을 삭제(소프트 삭제)합니다.
     * 실제 데이터 삭제가 아닌, 상태를 DELETED로 변경합니다.
     * @param productId 삭제할 상품의 고유 ID
     * @throws RuntimeException 상품을 찾을 수 없을 경우
     */
    @Override
    @Transactional // 쓰기 작업이므로 readOnly = true를 오버라이드하여 트랜잭션 활성화
    public void deleteProduct(Long productId) {
        // 삭제할 상품 엔티티를 조회합니다. 없으면 예외 발생.
        ProductEntity productToDelete = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("삭제할 상품을 찾을 수 없습니다. (ID: " + productId + ")"));

        productToDelete.setStatus(ProductStatus.DELETED);
        productRepository.save(productToDelete); // 트랜잭션 커밋 시 변경 감지되어 업데이트.
        // 물리적 삭제를 원한다면 productRepository.deleteById(productId); 를 사용합니다.
    }
}
