use naver_market;

-- product_detail_image 테이블 생성
CREATE TABLE product_detail_image (
    detail_image_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '상세 이미지의 고유 식별자',
    product_id BIGINT NOT NULL COMMENT '이 상세 이미지가 속한 상품의 ID',
    image_url VARCHAR(255) NOT NULL COMMENT '상세 이미지 파일의 URL 경로',
    image_order INT DEFAULT 0 COMMENT '상품 상세 이미지의 표시 순서 (낮을수록 우선)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '이미지 정보가 생성된 시간',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '이미지 정보가 마지막으로 업데이트된 시간',
    
    -- product 테이블의 product_id를 참조하는 외래 키 설정
    FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE
) COMMENT='상품 상세 이미지를 저장하는 테이블';

-- 인덱스 추가 (선택 사항): product_id로 조회하는 경우가 많으므로 인덱스를 추가하면 성능에 유리합니다.
CREATE INDEX idx_product_detail_image_product_id ON product_detail_image (product_id);

-- product_detail_image 테이블에서 created_at 컬럼 삭제
ALTER TABLE product_detail_image
DROP COLUMN created_at;

-- product_detail_image 테이블에서 updated_at 컬럼 삭제
ALTER TABLE product_detail_image
DROP COLUMN updated_at;

select * from product_detail_image;

-- (선택 사항) 테스트 데이터 삽입 예시
INSERT INTO product_detail_image (product_id, image_url, image_order) VALUES
(1, 'https://dnvefa72aowie.cloudfront.net/origin/article/202404/b8fde138d7be78385e1caf3938d8719432ed48265cee1628dbb474137c9269bc.jpg', 0),
(1, 'https://dnvefa72aowie.cloudfront.net/origin/article/202404/718f6ff15a454dc822f21900d496670baf4f8824d748aeedef6b93166aec9058.jpg', 1),
(1, 'https://dnvefa72aowie.cloudfront.net/origin/article/202404/db07f2a3299425f272e4ce8648268c35d98ff3d6e18b0521a2e8936f17be4beb.jpg', 2),
(1, 'https://dnvefa72aowie.cloudfront.net/origin/article/202404/f491a1a3a8144f02b40d8ac35788b285b721f1e4dce255233f3245f9a2a62ed7.jpg', 3),
(1, 'https://dnvefa72aowie.cloudfront.net/origin/article/202404/14b21e40c2c2cc493ba05a245dafeff7dbba6cd489d540527892c95d5b931eb0.jpg', 4),
(1, 'https://dnvefa72aowie.cloudfront.net/origin/article/202404/fdcc38e96c37847767c70d658080d6c15763825f388fe096b8f38bace150efc6.jpg', 5),
(1, 'https://dnvefa72aowie.cloudfront.net/origin/article/202404/94f2f81955e529d3e6705945c815f4aebb6d5bf309fb3e690fb37a429d612dda.jpg', 6),
(1, 'https://dnvefa72aowie.cloudfront.net/origin/article/202404/5c10272baf6eb5b75b63873489b96dab63618aab6026287fbcecebc313a7b8af.jpg', 7),
(1, 'https://dnvefa72aowie.cloudfront.net/origin/article/202404/86d72ca04c55c201fea6ef58e05be7dc551c0d36eb4040edecad13cb842badfd.jpg', 8);