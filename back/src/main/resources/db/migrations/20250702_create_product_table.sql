USE naver_market;

CREATE TABLE product (
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '상품 고유 ID',
    user_id BIGINT NOT NULL COMMENT '판매자 회원 ID (member 테이블 참조)',
    title VARCHAR(255) NOT NULL COMMENT '게시글 제목',
    category VARCHAR(100) NOT NULL COMMENT '상품 카테고리 (예: 의류, 전자제품, 도서 등)',
    price DECIMAL(10, 0) NOT NULL COMMENT '상품 가격 (원 단위, 소수점 없음)',
    description TEXT NOT NULL COMMENT '상세 설명',
    image_url VARCHAR(255) NOT NULL COMMENT '상품 대표 사진 URL (여러 장일 경우 별도 테이블 고려)',
    
    -- 상품 상태 (예: 판매중, 예약중, 판매완료)
    status ENUM('active', 'reserved', 'sold_out', 'deleted') NOT NULL DEFAULT 'active' COMMENT '상품 판매 상태',
    
    -- 조회수 (선택 사항)
    views INT DEFAULT 0 COMMENT '조회수',
    
    -- 생성 및 업데이트 타임스탬프
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '상품 등록 시간',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '마지막 업데이트 시간',
    
    -- 외부 키 제약 조건 (판매자 ID가 member 테이블의 member_id를 참조)
    CONSTRAINT fk_seller_id FOREIGN KEY (user_id) REFERENCES member(member_id)
    ON DELETE CASCADE ON UPDATE CASCADE
) COMMENT '상품 등록 테이블';

-- 인덱스 추가 (선택 사항, 검색 성능 향상에 도움)
CREATE INDEX idx_product_user_id ON product (user_id);
CREATE INDEX idx_product_category ON product (category);
CREATE INDEX idx_product_title ON product (title);