package dev.seo.navermarket.service.impl;

import dev.seo.navermarket.product.domain.ProductEntity;
import dev.seo.navermarket.product.domain.ProductStatus;
import dev.seo.navermarket.product.domain.ProductDetailImage;
import dev.seo.navermarket.dto.ProductCreateRequestDto;
import dev.seo.navermarket.dto.ProductDetailResponseDto;
import dev.seo.navermarket.dto.ProductListResponseDto;
import dev.seo.navermarket.dto.ProductUpdateRequestDto;
import dev.seo.navermarket.member.domain.MemberEntity;
import dev.seo.navermarket.repository.ProductRepository;
import dev.seo.navermarket.repository.MemberRepository;
import dev.seo.navermarket.repository.ProductDetailImageRepository;
import dev.seo.navermarket.service.FileStorageService;
import dev.seo.navermarket.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final MemberRepository memberRepository;
    private final FileStorageService fileStorageService;

    // 상품 목록 조회
    @Override
    public Page<ProductListResponseDto> getProducts(
            int page, int size, String sortBy, String direction,
            String status, String categoryName, Long memberId, String title,
            boolean defaultOrder) {

        Pageable pageable;
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        pageable = PageRequest.of(page, size, sort);

        ProductStatus productStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                productStatus = ProductStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("유효하지 않은 상품 상태입니다: " + status);
            }
        }

        // Repository에 동적 쿼리 메서드 findProductsByCriteria 구현 필요
        // 현재는 모든 상품을 가져오는 것으로 대체
        Page<ProductEntity> productEntities;
        if (defaultOrder) {
            // 예시: 최신순으로 정렬 (실제 로직은 Repository에서 구현)
            productEntities = productRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        } else {
            // 필터링 및 정렬 적용
            // 이 부분은 실제 복잡한 동적 쿼리 로직이 필요하며, Querydsl 또는 Specification 등을 사용.
            // 여기서는 간단히 findAll을 사용하며, 실제 필터링은 Repository에서 구현되어야 합니다.
            productEntities = productRepository.findAll(pageable);
        }

        // 엔티티 리스트를 DTO 리스트로 변환합니다. (DTO의 fromEntity 팩토리 메서드 사용)
        List<ProductListResponseDto> dtoList = productEntities.getContent().stream()
                .map(ProductListResponseDto::fromEntity)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, productEntities.getTotalElements());
    }
    
 // 상품 상세 조회
    @Override
    public Optional<ProductDetailResponseDto> getProductDetail(Long productId) {
        // Repository에서 상품 ID로 상세 이미지까지 함께 Fetch Join하여 가져옵니다.
        // TODO: findByIdWithDetailImages 메서드 ProductRepository에 구현 필요 (성능 최적화용)
        return productRepository.findById(productId)
                // 조회된 엔티티가 있다면 DTO로 변환하여 Optional로 감싸 반환합니다.
                .map(ProductDetailResponseDto::fromEntity);
    }
    
 // 상세 보기 조회수 증가 ( + 1)
    @Override
    @Transactional // 쓰기 작업이므로 readOnly = true를 오버라이드하여 트랜잭션 활성화
	public void incrementViewCount(Long productId) {
		ProductEntity product = productRepository.findById(productId)
				.orElseThrow(() -> new EntityNotFoundException("조회수를 증가시킬 상품을 찾을 수 없습니다. (ID: " + productId + ")"));
		product.incrementViewCount();
		// JPA의 Dirty Checking 덕분에 save()를 명시적으로 호출하지 않아도 트랜잭션 종료 시
		// 변경 사항이 반영됩니다.
	}

    // 상품 등록
    @Override
    @Transactional // 쓰기 작업이므로 readOnly = true를 오버라이드하여 트랜잭션 활성화
    public ProductDetailResponseDto createProduct(ProductCreateRequestDto requestDto, Long sellerId) {
    	log.info("상품 등록 요청 수신 - 판매자 ID: {}, 제목: {}", sellerId, requestDto.getTitle());
    	
        // 1. 판매자 정보 조회 (MemberEntity 가져오기)
        MemberEntity seller = memberRepository.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("판매자를 찾을 수 없습니다. ID: " + sellerId));

        // 2. 메인 이미지 저장 및 URL 획득
        String mainImageUrl;
        try {
            mainImageUrl = fileStorageService.storeFile(requestDto.getMainImage());
            log.info("메인 이미지 저장 완료: {}", mainImageUrl);
        } catch (IOException e) {
            log.error("메인 이미지 저장 실패: {}", e.getMessage(), e);
            throw new RuntimeException("메인 이미지 저장에 실패했습니다.", e);
        }
    				
        // 3. ProductEntity를 빌드합니다.
        ProductEntity product = ProductEntity.builder()
                .seller(seller) // MemberEntity seller 설정
                .title(requestDto.getTitle())
                .category(requestDto.getCategory())
                .price(requestDto.getPrice())
                .description(requestDto.getDescription())
                .mainImageUrl(mainImageUrl)
                .preferredTradeLocation(requestDto.getPreferredTradeLocation())
                .status(ProductStatus.ACTIVE) // 초기 상품 상태는 'ACTIVE'
                .views(0) // 초기 조회수는 0
                // createdAt, updatedAt은 AuditingEntityListener가 자동 처리
                .build();

        // 4. ProductEntity를 데이터베이스에 저장하고, 자동 생성된 productId를 포함한 엔티티를 받습니다.
        ProductEntity savedProduct = productRepository.save(product);
        log.info("상품 저장 완료 - productId: {}", savedProduct.getProductId());

        // 5. 상세 이미지 저장 로직
        if (requestDto.getDetailImages() != null && !requestDto.getDetailImages().isEmpty()) {
            int imageOrder = 0; // 상세 이미지 순서 (0부터 시작)
            for (MultipartFile detailFile : requestDto.getDetailImages()) {
                if (!detailFile.isEmpty()) {
                    try {
                        String detailImageUrl = fileStorageService.storeFile(detailFile);
                        ProductDetailImage detailImage = ProductDetailImage.builder()
                                .product(savedProduct) // 저장된 상품 엔티티와 연관 관계 설정
                                .imageUrl(detailImageUrl)
                                .imageOrder(imageOrder++) // 순서 부여 후 증가
                                .build();
                        savedProduct.addDetailImage(detailImage); // ProductEntity의 detailImages 컬렉션에 추가 (양방향 관계)
                        log.info("상세 이미지 저장 완료: {}", detailImageUrl);
                    } catch (IOException e) {
                        log.warn("상세 이미지 저장 실패 (건너뛰기): {}", e.getMessage());
                    }
                }
            }
            // 모든 상세 이미지를 한 번의 배치 작업으로 저장하여 성능을 최적화합니다.
            productDetailImageRepository.saveAll(savedProduct.getDetailImages());
            log.info("{}개의 상세 이미지 저장 완료", savedProduct.getDetailImages().size());
        }
        
        // 6. 생성된 상품 정보를 DTO로 변환하여 반환합니다.
        return ProductDetailResponseDto.fromEntity(savedProduct);
    }

    // 상품 수정 (상세 보기 수정)
    @Override
    @Transactional // 쓰기 작업이므로 readOnly = true를 오버라이드하여 트랜잭션 활성화
    public ProductDetailResponseDto updateProduct(Long productId, ProductUpdateRequestDto requestDto, Long sellerId) {
    	log.info("상품 업데이트 요청 수신 - 상품 ID: {}, 판매자 ID: {}", productId, sellerId);
    	
        // 1. 수정할 상품 엔티티를 조회합니다.
        ProductEntity existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("수정할 상품을 찾을 수 없습니다. (ID: " + productId + ")"));

        // 2. 보안 검증: 요청한 판매자 ID와 상품의 판매자 ID가 일치하는지 확인
        if (!existingProduct.getSeller().getMemberId().equals(sellerId)) {
            log.warn("상품 수정 권한 없음: 요청 판매자 ID={} != 상품 판매자 ID={}", sellerId, existingProduct.getSeller().getMemberId());
            throw new SecurityException("상품 수정 권한이 없습니다.");
        }

        // 3. 메인 이미지 업데이트 (새로운 이미지가 제공된 경우)
        String newMainImageUrl = existingProduct.getMainImageUrl(); // 기본적으로 기존 URL 유지
        if (requestDto.getMainImage() != null && !requestDto.getMainImage().isEmpty()) {
            try {
                // 기존 메인 이미지 파일 삭제 (선택 사항: 파일 시스템에서 실제 삭제)
                if (existingProduct.getMainImageUrl() != null && !existingProduct.getMainImageUrl().isEmpty()) {
                    fileStorageService.deleteFile(existingProduct.getMainImageUrl());
                }
                newMainImageUrl = fileStorageService.storeFile(requestDto.getMainImage());
                log.info("메인 이미지 업데이트 완료: {}", newMainImageUrl);
            } catch (IOException e) {
                log.error("메인 이미지 업데이트 실패: {}", e.getMessage(), e);
                throw new RuntimeException("메인 이미지 업데이트에 실패했습니다.", e);
            }
        }

        // 4. 상품 기본 정보 업데이트 (ProductEntity의 updateProduct 메서드 활용)
        existingProduct.updateProduct(
                requestDto.getTitle() != null ? requestDto.getTitle() : existingProduct.getTitle(),
                requestDto.getDescription() != null ? requestDto.getDescription() : existingProduct.getDescription(),
                requestDto.getPrice() != null ? requestDto.getPrice() : existingProduct.getPrice(),
                requestDto.getCategory() != null ? requestDto.getCategory() : existingProduct.getCategory(),
                newMainImageUrl, // 업데이트된 메인 이미지 URL
                requestDto.getPreferredTradeLocation() != null ? requestDto.getPreferredTradeLocation() : existingProduct.getPreferredTradeLocation()
        );
        // updatedAt은 Auditing이 자동 처리하므로 수동 설정 제거

        // 5. 상세 이미지 업데이트 로직
        // 기존 상세 이미지 파일 및 엔티티 삭제
        for (ProductDetailImage oldDetailImage : new ArrayList<>(existingProduct.getDetailImages())) {
            fileStorageService.deleteFile(oldDetailImage.getImageUrl());
            existingProduct.removeDetailImage(oldDetailImage);
        }
        log.info("기존 상세 이미지 모두 삭제 완료.");

     // 새로운 상세 이미지 저장 및 엔티티 추가
        if (requestDto.getDetailImages() != null && !requestDto.getDetailImages().isEmpty()) {
            int imageOrder = 0;
            for (MultipartFile detailFile : requestDto.getDetailImages()) { 
                if (!detailFile.isEmpty()) {
                    try {
                        String detailImageUrl = fileStorageService.storeFile(detailFile);
                        ProductDetailImage detailImage = ProductDetailImage.builder()
                                .product(existingProduct)
                                .imageUrl(detailImageUrl)
                                .imageOrder(imageOrder++)
                                .build();
                        existingProduct.addDetailImage(detailImage);
                        log.info("새 상세 이미지 저장 완료: {}", detailImageUrl);
                    } catch (IOException e) {
                        log.warn("새 상세 이미지 저장 실패 (건너뛰기): {}", e.getMessage());
                    }
                }
            }
            productDetailImageRepository.saveAll(existingProduct.getDetailImages());
            log.info("{}개의 새 상세 이미지 저장 완료.", existingProduct.getDetailImages().size());
        }
        
        // 6. 변경된 ProductEntity를 저장합니다.
        ProductEntity updatedProduct = productRepository.save(existingProduct);
        log.info("상품 업데이트 완료 - productId: {}", updatedProduct.getProductId());

        return ProductDetailResponseDto.fromEntity(updatedProduct);
    }

 // 상품 삭제 (soft delete)
    @Override
    @Transactional
    public void deleteProduct(Long productId, Long sellerId) {
        log.info("상품 삭제 요청 수신 - 상품 ID: {}, 판매자 ID: {}", productId, sellerId);
        
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("삭제할 상품을 찾을 수 없습니다. (ID: " + productId + ")"));

        // 보안 검증: 요청한 판매자 ID와 상품의 판매자 ID가 일치하는지 확인
        if (!product.getSeller().getMemberId().equals(sellerId)) {
            log.warn("상품 삭제 권한 없음: 요청 판매자 ID={} != 상품 판매자 ID={}", sellerId, product.getSeller().getMemberId());
            throw new SecurityException("상품 삭제 권한이 없습니다.");
        }

        // 상품 상태를 DELETED로 변경 (소프트 삭제)
        product.setStatus(ProductStatus.DELETED);
        productRepository.save(product);
        log.info("상품 소프트 삭제 성공 - 상품 ID: {}", productId);

        // TODO: 실제 파일 시스템에서 이미지 파일들을 삭제할지 여부는 정책에 따라 결정
        // 보통 소프트 삭제 시에는 파일을 즉시 삭제하지 않고, 일정 기간 후 배치 작업으로 삭제합니다.
    }

}