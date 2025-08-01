/**
 * @file types/product.ts
 * @brief 백엔드 API와 통신하기 위한 프론트엔드 데이터 타입 정의 파일입니다.
 * ProductListResponseDto, ProductDetailResponseDto, 그리고 페이지네이션을 위한 Page 타입을 정의합니다.
 */

/**
 * @interface ProductCreateRequest
 * @brief 새로운 상품 등록 요청 시 사용되는 DTO 타입입니다.
 * 백엔드의 ProductCreateRequestDto와 일치합니다.
 */
export type ProductCreateRequest = {
  title: string;
  category: string;
  price: number;
  description: string;
  preferredTradeLocation: string;
  // 백엔드 ProductCreateRequestDto에 추가 필드가 있다면 여기에 추가
};

/**
 * @interface ProductListResponseDto
 * @brief 상품 목록 조회 시 사용되는 DTO 타입입니다.
 * 백엔드의 ProductListResponseDto와 일치합니다.
 */
export interface ProductListResponseDto {
  productId: number;
  memberId: number;
  title: string;
  category: string;
  price: number;
  mainImageUrl: string;
  status: 'ACTIVE' | 'RESERVED' | 'SOLD_OUT' | 'DELETED';
  views: number;
  createdAt: string; // ISO 8601 형식의 날짜 문자열
  updatedAt: string; // ISO 8601 형식의 날짜 문자열
}

/**
 * @interface ProductDetailResponseDto
 * @brief 상품 상세 조회 시 사용되는 DTO 타입입니다.
 * 백엔드의 ProductDetailResponseDto와 일치합니다.
 */
export interface ProductDetailResponseDto {
  productId: number;
  memberId: number;
  title: string;
  category: string;
  price: number;
  description: string;
  imageUrls: string[]; // 모든 상세 이미지 URL 목록
  status: 'ACTIVE' | 'RESERVED' | 'SOLD_OUT' | 'DELETED';
  views: number;
  createdAt: string;
  updatedAt: string;
}

/**
 * @interface Page
 * @brief Spring Data JPA Page 객체에 대응하는 제네릭 타입입니다.
 * 백엔드에서 페이지네이션된 데이터를 받을 때 사용됩니다.
 * @template T 페이지의 내용을 구성하는 요소의 타입
 */
export interface Page<T> {
  content: T[]; // 실제 데이터 목록
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      empty: boolean;
      sorted: boolean;
      unsorted: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}

// 백엔드 에러 응답 DTO에 해당하는 타입
export type ProductErrorResponse = {
  status: number;
  message: string;
  code: string;
  // 에러 응답 DTO에 따라 추가 필드 가능 (예: errors: { field: string, defaultMessage: string }[])
};