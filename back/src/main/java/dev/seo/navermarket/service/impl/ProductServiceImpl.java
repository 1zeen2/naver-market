package dev.seo.navermarket.service.impl;

import dev.seo.navermarket.product.domain.ProductEntity;
import dev.seo.navermarket.product.domain.ProductDetailImage;
import dev.seo.navermarket.dto.ProductCreateRequestDto;
import dev.seo.navermarket.dto.ProductDetailImageRequestDto;
import dev.seo.navermarket.dto.ProductDetailResponseDto;
import dev.seo.navermarket.dto.ProductListResponseDto;
import dev.seo.navermarket.dto.ProductUpdateRequestDto;
import dev.seo.navermarket.entity.ProductStatus;
import dev.seo.navermarket.repository.ProductRepository;
import dev.seo.navermarket.repository.ProductDetailImageRepository;
import dev.seo.navermarket.service.ProductService; // ProductService 인터페이스 임포트
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @file ProductServiceImpl.java
 * @brief ProductService 인터페이스의 구현체입니다.
 * 상품 관련 비즈니스 로직을 실제로 처리하며, ProductRepository 및 ProductDetailImageRepository와 상호작용합니다.
 * 모든 조회 메서드는 DTO를 반환하며, 페이지네이션 및 다양한 필터링을 지원합니다.
 * 상품 등록/수정 시 상세 이미지 목록을 함께 처리합니다.
 */
@Service
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성하여 의존성 주입 (ProductRepository, ProductDetailImageRepository)
@Transactional(readOnly = true) // 클래스 레벨에서 모든 메서드에 읽기 전용 트랜잭션을 적용합니다.
public class ProductServiceImpl implements ProductService {
	
	private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final ProductDetailImageRepository productDetailImageRepository;

    // 상품 목록 조회
    @Override
    public Page<ProductListResponseDto> getProducts(
            int page, int size, String sortBy, String direction,
            String status, String categoryName, Long memberId, String title,
            boolean defaultOrder) {

        Pageable pageable;
        if (defaultOrder) {
            // 메인 페이지 기본 정렬 로직 (Repository에서 정의된 쿼리 사용)
            pageable = PageRequest.of(page, size);
            Page<ProductEntity> productEntities = productRepository.findAllActiveAndOrdered(pageable);
            List<ProductListResponseDto> dtoList = productEntities.getContent().stream()
                    .map(this::convertToProductListResponseDto)
                    .collect(Collectors.toList());
            return new PageImpl<>(dtoList, pageable, productEntities.getTotalElements());
        } else {
            // 일반 검색/필터링 정렬 로직
            Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
            pageable = PageRequest.of(page, size, sort);
        }

        ProductStatus productStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                productStatus = ProductStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // 유효하지 않은 상품 상태가 전달될 경우 예외 발생
                throw new IllegalArgumentException("유효하지 않은 상품 상태입니다: " + status);
            }
        }

        // Repository의 동적 쿼리 메서드를 호출하여 상품 엔티티 페이지를 가져옵니다.
        Page<ProductEntity> productEntities = productRepository.findProductsByCriteria(
                productStatus, categoryName, memberId, title, pageable);

        // 엔티티 리스트를 DTO 리스트로 변환합니다.
        List<ProductListResponseDto> dtoList = productEntities.getContent().stream()
                .map(this::convertToProductListResponseDto)
                .collect(Collectors.toList());

        // 변환된 DTO 리스트와 페이지 정보를 포함하는 Page 객체를 반환합니다.
        return new PageImpl<>(dtoList, pageable, productEntities.getTotalElements());
    }
    
    /**
     * ProductEntity를 ProductDetailResponseDto로 변환합니다.
     * 상품 상세 조회 시 사용되며, 모든 상세 이미지 URL을 포함합니다.
     *
     * @param productEntity 변환할 ProductEntity
     * @return ProductDetailResponseDto
     */
    private ProductDetailResponseDto convertToProductDetailResponseDto(ProductEntity productEntity) {
        // 모든 상세 이미지 URL을 imageOrder 순서대로 정렬하여 리스트로 만듭니다.
        List<String> imageUrls = productEntity.getDetailImages().stream()
                .sorted((img1, img2) -> img1.getImageOrder().compareTo(img2.getImageOrder())) // imageOrder 순으로 정렬
                .map(ProductDetailImage::getImageUrl)
                .collect(Collectors.toList());

        return ProductDetailResponseDto.builder()
                .productId(productEntity.getProductId())
                .memberId(productEntity.getMemberId())
                .title(productEntity.getTitle())
                .category(productEntity.getCategory())
                .price(productEntity.getPrice())
                .description(productEntity.getDescription())
                .imageUrls(imageUrls) // 모든 이미지 URL 설정
                .status(productEntity.getStatus())
                .views(productEntity.getViews())
                .createdAt(productEntity.getCreatedAt())
                .updatedAt(productEntity.getUpdatedAt())
                .build();
    }

    // 상세보기 + 상세 이미지 포함
    @Override
    public Optional<ProductDetailResponseDto> getProductDetail(Long productId) {
        // Repository에서 상품 ID로 상세 이미지까지 함께 Fetch Join하여 가져옵니다.
        return productRepository.findByIdWithDetailImages(productId)
                // 조회된 엔티티가 있다면 DTO로 변환하여 Optional로 감싸 반환합니다.
                .map(this::convertToProductDetailResponseDto);
    }
    
    // 상세 보기 조회수 증가 ( + 1)
    @Override
    @Transactional // 쓰기 작업이므로 readOnly = true를 오버라이드하여 트랜잭션 활성화
	public void incrementViewCount(Long productId) {
		ProductEntity product = productRepository.findById(productId)
				.orElseThrow(() -> new RuntimeException("조회수를 증가시킬 상품을 찾을 수 없습니다. (ID: " + productId + ")"));
		product.incrementViewCount();
		// JPA의 Dirty Checking 덕분에 save()를 명시적으로 호출하지 않아도 트랜잭션 종료 시
		// 변경 사항이 반영될 수 있지만 명시적으로 save()를 안전하게 호출
		productRepository.save(product);
	}

    // 상품 등록
    @Override
    @Transactional // 쓰기 작업이므로 readOnly = true를 오버라이드하여 트랜잭션 활성화
    public ProductListResponseDto createProduct(ProductCreateRequestDto productDto) {
    	log.info("상품 등록 요청 수신 - title: {}", productDto.getTitle());
    	
    	// DTO의 유효성 검사(@NotNull, @Size(min=1)) 덕분에 detailImages는 항상 비어있지 않음.
        // 썸네일 이미지(imageOrder=0)가 있는지 확인 (필수 조건)
        boolean hasThumbnail = productDto.getDetailImages().stream()
                                .anyMatch(img -> img.getImageOrder() == 0);
        if (!hasThumbnail) {
            throw new IllegalArgumentException("상품 썸네일 이미지는 필수입니다.");
        }
    				
        // DTO로부터 ProductEntity를 빌드합니다.
        ProductEntity product = ProductEntity.builder()
                .memberId(productDto.getMemberId()) // 컨트롤러에서 설정된 memberId 사용
                .title(productDto.getTitle())
                .category(productDto.getCategory())
                .price(productDto.getPrice())
                .description(productDto.getDescription())
                .status(productDto.getStatus() != null ? productDto.getStatus() : ProductStatus.ACTIVE) // 기본 상태 ACTIVE
                .views(productDto.getViews() != null ? productDto.getViews() : 0) // 기본 조회수 0
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // ProductEntity를 데이터베이스에 저장하고, 자동 생성된 productId를 포함한 엔티티를 받습니다.
        ProductEntity savedProduct = productRepository.save(product);
        log.info("상품 저장 완료 - productId: {}", savedProduct.getProductId());

        // 상세 이미지 저장 로직
        // productDto.getDetailImages()는 @Size(min=1)에 의해 항상 최소 1개 이상 보장됨
        for (int i = 0; i < productDto.getDetailImages().size(); i++) {
            ProductDetailImageRequestDto imageRequest = productDto.getDetailImages().get(i);
            ProductDetailImage detailImage = ProductDetailImage.builder()
                    .product(savedProduct) // 저장된 상품 엔티티와 연관 관계 설정
                    .imageUrl(imageRequest.getImageUrl())
                    .imageOrder(i) // 리스트의 인덱스를 이미지 순서(0부터 시작)로 사용
                    .build();
            savedProduct.addDetailImage(detailImage); // ProductEntity의 detailImages 컬렉션에 추가 (양방향 관계)
        }
        // 모든 상세 이미지를 한 번의 배치 작업으로 저장하여 성능을 최적화합니다.
        productDetailImageRepository.saveAll(savedProduct.getDetailImages());
        log.info("{}개의 상세 이미지 저장 완료", savedProduct.getDetailImages().size());
        
        // 생성된 상품 정보를 DTO로 변환하여 반환합니다.
        return convertToProductListResponseDto(savedProduct);
    }

    // 상품 수정 (상세 보기 수정)
    @Override
    @Transactional // 쓰기 작업이므로 readOnly = true를 오버라이드하여 트랜잭션 활성화
    public ProductListResponseDto updateProduct(Long productId, ProductUpdateRequestDto productDto) {
    	log.info("상품 업데이트 요청 수신 - productId: {}", productId);
    	
        // 수정할 상품 엔티티를 조회합니다. 상세 이미지까지 함께 Fetch Join하여 가져옵니다.
        ProductEntity existingProduct = productRepository.findByIdWithDetailImages(productId)
                .orElseThrow(() -> new RuntimeException("수정할 상품을 찾을 수 없습니다. (ID: " + productId + ")"));

        // DTO의 값으로 상품의 기본 정보를 업데이트합니다.
        if (productDto.getTitle() != null) existingProduct.setTitle(productDto.getTitle());
        if (productDto.getCategory() != null) existingProduct.setCategory(productDto.getCategory());
        if (productDto.getPrice() != null) existingProduct.setPrice(productDto.getPrice());
        if (productDto.getDescription() != null) existingProduct.setDescription(productDto.getDescription());
        if (productDto.getStatus() != null) existingProduct.setStatus(productDto.getStatus());
        if (productDto.getViews() != null) existingProduct.setViews(productDto.getViews());
        existingProduct.setUpdatedAt(LocalDateTime.now());

        // 상세 이미지 업데이트 로직
        // 기존 상세 이미지를 모두 삭제합니다. (orphanRemoval=true 설정으로 DB에서도 삭제)
        productDetailImageRepository.deleteByProduct_ProductId(existingProduct.getProductId());
        existingProduct.getDetailImages().clear(); // 엔티티 컬렉션도 비워줍니다.

     // 썸네일 이미지(imageOrder=0)가 있는지 확인 (필수 조건)
        boolean hasThumbnail = productDto.getDetailImages().stream()
                                .anyMatch(img -> img.getImageOrder() == 0);
        if (!hasThumbnail) {
            throw new IllegalArgumentException("상품 썸네일 이미지는 필수 입니다.");
        }
        
        // 새로운 이미지 목록이 비어있지 않다면, 새 이미지들을 저장합니다.
        for (int i = 0; i < productDto.getDetailImages().size(); i++) {
            ProductDetailImageRequestDto imageRequest = productDto.getDetailImages().get(i);
            ProductDetailImage detailImage = ProductDetailImage.builder()
                    .product(existingProduct)
                    .imageUrl(imageRequest.getImageUrl())
                    .imageOrder(i) // 리스트 인덱스를 순서로 사용
                    .build();
            existingProduct.addDetailImage(detailImage); // ProductEntity의 컬렉션에 추가
        }
        // 모든 새 상세 이미지를 한 번에 저장합니다.
        productDetailImageRepository.saveAll(existingProduct.getDetailImages());
        log.info("{}개의 상세 이미지 업데이트 완료.", existingProduct.getDetailImages().size());
        
        // 변경된 ProductEntity를 저장합니다. (JPA Dirty Checking에 의해 자동 업데이트)
        ProductEntity updatedProduct = productRepository.save(existingProduct);
        log.info("상품 업데이트 완료 - productId: {}", updatedProduct.getProductId());
        return convertToProductListResponseDto(updatedProduct);
    }

 // 상세 보기 삭제 (soft delete)
    @Override
    @Transactional // 쓰기 작업이므로 readOnly = true를 오버라이드하여 트랜잭션 활성화
    public void deleteProduct(Long productId) {
        log.info("상품 삭제 요청 수신 - productId: {}", productId);
        // 상품 엔티티를 조회하여 존재 여부를 확인합니다.
        if (!productRepository.existsById(productId)) {
            throw new RuntimeException("삭제할 상품을 찾을 수 없습니다. (ID: " + productId + ")");
        }
        // ProductEntity 삭제 시, ProductDetailImage는 cascade = CascadeType.ALL, orphanRemoval = true 설정에 의해
        // 자동으로 연관된 상세 이미지들도 삭제됩니다.
        productRepository.deleteById(productId);
        log.info("상품 삭제 완료 - productId: {}", productId);
    }

    // --- DTO 변환 헬퍼 메서드 ---

    /**
     * ProductEntity를 ProductListResponseDto로 변환합니다.
     * 상품 목록 조회 시 사용되며, 대표 이미지(imageOrder=0) URL을 포함합니다.
     *
     * @param productEntity 변환할 ProductEntity
     * @return ProductListResponseDto
     */
    private ProductListResponseDto convertToProductListResponseDto(ProductEntity productEntity) {
        // 대표 이미지 URL을 ProductDetailImage 컬렉션에서 찾습니다.
        String mainImageUrl = productEntity.getDetailImages().stream()
                .filter(img -> img.getImageOrder() == 0)
                .map(ProductDetailImage::getImageUrl)
                .findFirst()
                .orElse("https://placehold.co/400x300/CCCCCC/333333?text=No+Image"); // 썸네일이 없을 경우 기본 이미지

        return ProductListResponseDto.builder()
                .productId(productEntity.getProductId())
                .memberId(productEntity.getMemberId())
                .title(productEntity.getTitle())
                .category(productEntity.getCategory())
                .price(productEntity.getPrice())
                .mainImageUrl(mainImageUrl) // 변환된 대표 이미지 URL 설정
                .status(productEntity.getStatus())
                .views(productEntity.getViews())
                .createdAt(productEntity.getCreatedAt())
                .updatedAt(productEntity.getUpdatedAt())
                .build();
    }

}