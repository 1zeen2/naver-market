USE naver_market;

-- product 테이블의 image_url 컬럼을 NOT NULL로 변경합니다.
-- 이 작업을 수행하기 전에, 해당 컬럼에 NULL 값이 없어야 합니다.

ALTER TABLE product
MODIFY COLUMN image_url VARCHAR(255) NOT NULL COMMENT '상품 대표 사진 URL';