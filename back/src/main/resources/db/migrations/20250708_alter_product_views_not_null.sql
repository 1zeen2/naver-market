USE naver_market;

-- product 테이블의 views 컬럼을 NOT NULL로 변경합니다.
-- 이 작업을 수행하기 전에, 해당 컬럼에 NULL 값이 없어야 합니다.
-- 만약 NULL 값이 있다면, 아래와 같이 0으로 후 작업을 수행해야 합니다.

ALTER TABLE product
MODIFY COLUMN views INT NOT NULL DEFAULT 0 COMMENT '조회수';
