USE naver_market;

-- product 테이블에서 image_url 컬럼 제거
ALTER TABLE product
DROP COLUMN image_url;

-- COMMENT: 상품의 모든 이미지는 product_detail_image 테이블에서 관리하도록 변경합니다.
-- COMMENT: 대표 이미지는 product_detail_image.image_order = 0 으로 지정됩니다.