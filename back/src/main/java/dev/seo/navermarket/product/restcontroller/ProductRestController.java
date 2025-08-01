package dev.seo.navermarket.product.restcontroller;

import dev.seo.navermarket.product.dto.ProductCreateRequestDto;
import dev.seo.navermarket.product.dto.ProductDetailResponseDto;
import dev.seo.navermarket.product.dto.ProductErrorResponseDto;
import dev.seo.navermarket.product.dto.ProductListResponseDto;
import dev.seo.navermarket.product.dto.ProductUpdateRequestDto;
import dev.seo.navermarket.product.service.ProductService;
import dev.seo.navermarket.security.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @file ProductRestController.java
 * @brief 상품 관련 REST API 요청을 처리하는 컨트롤러 클래스입니다.
 * 프론트엔드로부터의 HTTP 요청을 받아 상품 데이터를 조회하고 응답합니다.
 * 모든 응답은 DTO 형태로 반환됩니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductRestController {
	
	private final ProductService productService;
	
	// 조회수 중복 방지를 위한 쿠키 만료 시간 (24시간)
	private static final long VIEW_COUNT_COOKIE_EXPIRATION_HOURS = 24;
	// 조회된 상품 ID를 저장할 쿠키의 이름
	private static final String VIEWED_PRODUCTS_COOKIE_NAME = "viewed_products";
	 
	 
	
	// --- 조회 (Read) EndPoint ---
	
	/**
     * @brief 메인 페이지 기본 표시용: DELETED 상태를 제외한 모든 상품을 최신 등록일 기준으로 조회합니다.
     * GET /api/products/main-feed
     * @param page 페이지 번호 (기본값 0)
     * @param size 페이지 크기 (기본값 10)
     * @param sortBy 정렬 기준 필드 (기본값 "createdAt")
     * @param direction 정렬 방향 (기본값 "desc")
     * @return ResponseEntity<Page<ProductListResponseDto>> 상품 DTO 페이지 리스트와 HTTP 상태 코드 (200 OK)
     */
    @GetMapping("/main-feed")
    public ResponseEntity<Page<ProductListResponseDto>> getMainFeedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        // defaultOrder를 true로 설정하여 메인 페이지용 기본 정렬 로직을 사용하도록 합니다.
        Page<ProductListResponseDto> products = productService.getProducts(
                page, size, sortBy, direction,
                null, null, null, null, true);
        return ResponseEntity.ok(products);
    }
	
    /**
     * @brief 상품 목록을 다양한 기준으로 조회하는 API 엔드포인트입니다.
     * 이 엔드포인트는 프론트엔드의 상태 필터링, 카테고리 필터링, 판매자별 조회, 제목 검색 등
     * 모든 일반적인 상품 목록 조회 시 사용됩니다.
     * GET /api/products
     * @param page 페이지 번호 (기본값 0)
     * @param size 페이지 크기 (기본값 10)
     * @param sortBy 정렬 기준 필드 (기본값 "createdAt")
     * @param direction 정렬 방향 (asc 또는 desc, 기본값 "desc")
     * @param status 조회할 상품 상태 이름 (ACTIVE, RESERVED, SOLD_OUT, DELETED), 선택 사항
     * @param categoryName 조회할 카테고리 이름, 선택 사항
     * @param memberId 판매자 회원 ID, 선택 사항
     * @param title 검색할 제목 (부분 일치), 선택 사항
     * @return ResponseEntity<Page<ProductListResponseDto>> 상품 DTO 페이지 리스트와 HTTP 상태 코드 (200 OK)
     */
    @GetMapping
    public ResponseEntity<Page<ProductListResponseDto>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String title) {

        // defaultOrder를 false로 설정하여 일반 검색/필터링 정렬 로직을 사용하도록 합니다.
        Page<ProductListResponseDto> products = productService.getProducts(
                page, size, sortBy, direction,
                status, categoryName, memberId, title, false);
        return ResponseEntity.ok(products);
    }

    /**
     * @brief 특정 상품 ID로 상품 상세 정보를 조회하는 API 엔드포인트입니다.
     * GET /api/products/{productId}
     * 이 엔드포인트 호출 시 조회수 증가 로직(중복 방지 포함)이 함께 처리됩니다.
     * @param productId 조회할 상품의 고유 ID
     * @param request HttpServletRequest 객체 (쿠키 읽기용)
     * @param response HttpServletResponse 객체 (쿠키 쓰기용)
     * @return ResponseEntity<ProductDetailResponseDto> 조회된 상품 DTO와 HTTP 상태 코드 (200 OK 또는 404 Not Found)
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponseDto> getProductDetail(
            @PathVariable Long productId, HttpServletRequest request, HttpServletResponse response) {
        
        log.info("상품 상세 페이지 조회 요청: productId={}", productId);
        
        // 조회수 증가 및 중복 방지 로직
        processProductView(productId, request, response);
        
        // 상품 상세 정보 조회
        Optional<ProductDetailResponseDto> productDetailDto = productService.getProductDetail(productId);
        return productDetailDto.map(ResponseEntity::ok)
                               .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    // --- CRUD (Create, Update, Delete) 엔드포인트 ---
    
    /**
     * @brief 새로운 상품을 등록하는 API 엔드포인트입니다.
     * POST /api/products
     * @param productCreateRequestDto 등록할 상품 정보가 담긴 DTO 객체 (프론트엔드에서 "request" 이름으로 전송)
     * @param mainImage 대표 이미지 파일
     * @param detailImages 상세 이미지 파일 리스트
     * @param currentUser 현재 로그인된 사용자 정보 (JWT 토큰에서 추출)
     * @return ResponseEntity<ProductDetailResponseDto> 등록된 상품 DTO와 HTTP 상태 코드 (201 Created)
     */
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createProduct( // 반환 타입을 ResponseEntity<?>로 변경하여 에러 DTO도 반환 가능하게 함
    		@RequestPart("request") @Valid ProductCreateRequestDto productDto,
            @RequestPart(value = "mainImage", required = true) MultipartFile mainImage,
            @RequestPart(value = "detailImages", required = false) List<MultipartFile> detailImages,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        // 1. 사용자 인증 확인
        if (currentUser == null) {
            log.warn("인증되지 않은 사용자가 상품 등록을 시도했습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(new ProductErrorResponseDto(HttpStatus.UNAUTHORIZED.value(), "로그인이 필요합니다.", "UNAUTHORIZED"));
        }

        // 3. 메인 이미지 파일 유효성 검사
        if (mainImage.isEmpty()) {
            log.error("메인 이미지 파일이 비어있습니다. 파일명: {}, 크기: {}",
            		mainImage.getOriginalFilename(),
            		mainImage.getSize());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            					 .body(new ProductErrorResponseDto(HttpStatus.BAD_REQUEST.value(), "대표 이미지를 등록해주세요.", "MAIN_IMAGE_EMPTY"));
        }
        log.info("메인 이미지 수신: 파일명={}, 크기={} bytes, ContentType={}", 
                 mainImage.getOriginalFilename(), mainImage.getSize(), mainImage.getContentType());
        
     // 4. 상세 이미지 파일 유효성 검사 (선택 사항이므로 null 체크 후 순회)
        if (detailImages != null && !detailImages.isEmpty()) {
            log.info("상세 이미지 {}개 수신", detailImages.size());
            for (MultipartFile file : detailImages) {
                if (file.isEmpty()) {
                    log.warn("비어있는 상세 이미지 파일 발견: 파일명={}", file.getOriginalFilename());
                } else {
                    log.info("상세 이미지: 파일명={}, 크기={} bytes, ContentType={}",
                             file.getOriginalFilename(), file.getSize(), file.getContentType());
                }
            }
            if (detailImages.size() > 9) {
            	log.error("상세 이미지가 최대 허용 개수(9개)를 초과했습니다. 개수: {}", detailImages.size());
            	return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			                         .body(new ProductErrorResponseDto(HttpStatus.BAD_REQUEST.value(), "상세 이미지는 최대 9장까지 등록 가능합니다.", "TOO_MANY_DETAIL_IMAGES"));
        	}
        } else {
            log.info("상세 이미지 없음.");
        }

        Long sellerId = currentUser.getMemberId(); // CustomUserDetails에서 getMember().getId()로 ID 가져오기
        log.info("상품 등록 요청 최종 확인 - 판매자 ID: {}, 제목: {}", sellerId, productDto.getTitle()); // 파싱된 DTO 사용

        // 5. 서비스 계층 호출 및 결과 반환
        try {
            ProductDetailResponseDto createdProduct = productService.createProduct(productDto, mainImage, detailImages, sellerId);
            log.info("상품이 성공적으로 등록되었습니다. 상품 ID: {}", createdProduct.getProductId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
        } catch (Exception e) {
            log.error("상품 생성 중 서비스 로직 오류 발생: {}", e.getMessage(), e); // 상세 예외 정보 로깅
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(new ProductErrorResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), "상품 등록 중 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", "PRODUCT_REGISTRATION_FAILED"));
        }
    }
    
    /**
     * @brief 기존 상품 정보를 수정하는 API 엔드포인트입니다.
     * PUT /api/products/{productId}
     * @param productId 수정할 상품의 고유 ID (경로 변수)
     * @param requestDto 수정할 상품 정보가 담긴 DTO (MultipartFile 포함)
     * @param currentUser 현재 로그인된 사용자 정보 (JWT 토큰에서 추출)
     * @return ResponseEntity<ProductDetailResponseDto> 수정된 상품 DTO와 HTTP 상태 코드 (200 OK 또는 404 Not Found)
     */
    @PutMapping(value = "/{productId}", consumes = {"multipart/form-data"})
    public ResponseEntity<ProductDetailResponseDto> updateProduct(
            @PathVariable Long productId,
            @RequestPart("request") ProductUpdateRequestDto requestDto,
            @RequestPart(value = "mainImage", required = false) MultipartFile mainImage,
            @RequestPart(value = "detailImages", required = false) List<MultipartFile> detailImages,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Long sellerId = currentUser.getMemberId();
        log.info("상품 업데이트 요청 수신 - 상품 ID: {}, 판매자 ID: {}", productId, sellerId);

        try {
            ProductDetailResponseDto updatedProduct = productService.updateProduct(
                    productId,
                    requestDto,
                    mainImage,
                    detailImages,
                    sellerId
            );
            return ResponseEntity.ok(updatedProduct);
        } catch (EntityNotFoundException e) {
            log.warn("상품 수정 실패: 상품을 찾을 수 없습니다. (ID: {})", productId);
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            log.error("상품 수정 권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("상품 수정 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * @brief 특정 상품을 삭제(소프트 삭제)하는 API 엔드포인트입니다.
     * DELETE /api/products/{productId}
     * @param productId 삭제할 상품의 고유 ID (경로 변수)
     * @param currentUser 현재 로그인된 사용자 정보 (JWT 토큰에서 추출)
     * @return ResponseEntity<Void> HTTP 상태 코드 (204 No Content 또는 404 Not Found)
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Long sellerId = currentUser.getMemberId(); // CustomUserDetails에 getMemberId() 메서드가 있다고 가정
        log.info("상품 삭제 요청 수신 - 상품 ID: {}, 판매자 ID: {}", productId, sellerId);

    	try {
    		productService.deleteProduct(productId, sellerId);
    		return ResponseEntity.noContent().build();
    	} catch (EntityNotFoundException e) {
            log.warn("상품 삭제 실패: 상품을 찾을 수 없습니다. (ID: {})", productId);
    		return ResponseEntity.notFound().build();
    	} catch (SecurityException e) {
            log.error("상품 삭제 권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("상품 삭제 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * @brief 상품 조회수 증가 및 중복 방지 로직을 처리하는 헬퍼 메서드입니다.
     * 클라이언트의 쿠키를 확인하여 특정 상품의 조회수가 24시간 이내에 이미 증가되었는지 판단합니다.
     * @param productId 조회수를 증가시킬 상품의 고유 ID
     * @param request HttpServletRequest 객체 (쿠키 읽기용)
     * @param response HttpServletResponse 객체 (쿠키 쓰기용)
     */
    private void processProductView(Long productId, HttpServletRequest request, HttpServletResponse response) {
    	log.debug("Starting processProductView for productId: {}", productId);
        Cookie[] cookies = request.getCookies();
        String viewedProductsString = "";
        Optional<Cookie> viewedCookie = Optional.empty();
    	
    	// 기존 쿠키에서 'viewed_products' 쿠키 값 가져오기
        if (cookies != null) {
            log.debug("Found {} cookies in request.", cookies.length);
            for (Cookie cookie : cookies) {
                log.debug("Cookie: Name={}, Value={}", cookie.getName(), cookie.getValue());
            }
            viewedCookie = Arrays.stream(cookies)
                    .filter(cookie -> VIEWED_PRODUCTS_COOKIE_NAME.equals(cookie.getName()))
                    .findFirst();
            if (viewedCookie.isPresent()) {
            	try {
            		// 쿠기 값을 읽을 때 URL 디코딩
            		viewedProductsString = URLDecoder.decode(viewedCookie.get().getValue(), StandardCharsets.UTF_8.toString());
            		log.debug("Existing '{}' cookie found with decoded value: {}", VIEWED_PRODUCTS_COOKIE_NAME, viewedProductsString);
            	} catch (UnsupportedEncodingException e) {
					log.error("Failed to decode cookie value: {}", viewedCookie.get().getValue(), e);
					// 디코딩 실패 시 빈 문자열로 처리하여 새로 시작하도록 함
					viewedProductsString = "";
				}
            } else {
                log.debug("No existing '{}' cookie found.", VIEWED_PRODUCTS_COOKIE_NAME);
            }
        } else {
            log.debug("No cookies found in request.");
        }
    	
    	// 현재 상품 ID가 이미 쿠키에 기록되어 있는지 확인 (조회된 적이 있는지)
    	// 쿠키 값은 "1, 2, 3" 과 같이 콤마로 구분된 ID 문자열로 가정
        boolean alreadyViewed = false;
        if (!viewedProductsString.isEmpty()) {
            alreadyViewed = Arrays.stream(viewedProductsString.split(","))
                                .anyMatch(id -> id.equals(String.valueOf(productId)));
            log.debug("Product ID {} already viewed in cookie: {}", productId, alreadyViewed);
        } else {
            log.debug("viewedProductsString is empty, so product ID {} is not in cookie.", productId);
        }
    	
    	// 조회된 적이 없다면 조회수 증가 및 쿠키 업데이트
        if (!alreadyViewed) {
            log.info("Incrementing view count for product ID: {}", productId);
            productService.incrementViewCount(productId); // 서비스 계층의 조회수 증가 메서드 호출

            // 쿠키 업데이트: 새 상품 ID를 추가
            String newViewedProductsString = viewedProductsString.isEmpty() ?
                                            String.valueOf(productId) :
                                            viewedProductsString + "," + productId;

            // 쿠키 값을 저장할 때 URL 인코딩
            try {
            	String encodedValue = URLEncoder.encode(newViewedProductsString, StandardCharsets.UTF_8.toString());
            	Cookie newCookie = new Cookie (VIEWED_PRODUCTS_COOKIE_NAME, encodedValue);
            	newCookie.setMaxAge((int) TimeUnit.HOURS.toSeconds(VIEW_COUNT_COOKIE_EXPIRATION_HOURS));
            	newCookie.setPath("/");
            	newCookie.setHttpOnly(true);
            	
            	response.addCookie(newCookie);
            	log.debug("New cookie '{}' set with encoded value: {}", VIEWED_PRODUCTS_COOKIE_NAME, encodedValue);
            } catch (UnsupportedEncodingException e) {
            	log.error("Failed to encode cookie value: {}", newViewedProductsString, e);
            }
        } else {
            log.info("Product ID {} already viewed, not incrementing view count.", productId);
        }
    }
}
