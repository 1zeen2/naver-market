package dev.seo.navermarket.entity;

/**
 * @file ProductStatus.java
 * @brief 상품의 판매 상태를 정의하는 Enum 클래스입니다.
 * ACTIVE: 판매중
 * RESERVED: 예약중
 * SOLD_OUT: 판매완료
 * DELETED: 삭제됨 (소프트 삭제)
 */
public enum ProductStatus {
    ACTIVE,
    RESERVED,
    SOLD_OUT, // ✨ SOLD__OUT 오타 수정
    DELETED
}
