// 백엔드의 ProductEntity와 일치하는 인터페이스를 정의합니다.
// Spring Boot의 LocalDateTime은 ISO 8601 문자열 (예: "2025-07-04T21:02:44")로 변환되어 넘어옵니다.

export interface Product {
  productId: number;
  memberId: number;
  title: string;
  price: number;
  category: string;
  description: string;
  imageUrl: string;
  status: string;
  views: number;
  createdAt: string;
  updatedAt: string;
}

// Spring Data JPA Page 객체 응답 구조에 맞춘 인터페이스
// 백엔드 ProductRestController에서 Page<ProductEntity>를 반환
export interface ProductResponse {
  content: Product[];
  totalElements: number;
  totalPages: number;
  number: number; // 현재 페이지 번호 (0부터 시작)
  size: number;   // 페이지당 아이템 수
  first: boolean;
  last: boolean;
  empty: boolean;
}
