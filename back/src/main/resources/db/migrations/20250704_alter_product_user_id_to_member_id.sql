USE naver_market;

-- 1. 기존 외부 키 제약 조건 삭제 (컬럼 이름 변경 전)
-- 'fk_seller_id'라는 이름의 외부 키가 'product.user_id'를 참조하고 있으므로,
-- 컬럼 이름 변경 전에 이 제약 조건을 먼저 삭제.
ALTER TABLE product
DROP FOREIGN KEY fk_seller_id;

-- 2. 'user_id' 컬럼 이름을 'member_id'로 변경
-- 컬럼의 데이터 타입(BIGINT)과 제약 조건(NOT NULL)은 유지하며 이름만 변경.
ALTER TABLE product
CHANGE COLUMN user_id member_id BIGINT NOT NULL COMMENT '판매자 회원 번호 (member_id)';

-- 3. 새로운 외부 키 제약 조건 추가
-- 'product.member_id' 컬럼이 'member.member_id'를 참조하도록 설정.
-- 새로운 외부 키 이름은 'fk_seller_id'로 명확하게 지정.
ALTER TABLE product
ADD CONSTRAINT fk_seller_id FOREIGN KEY (member_id) REFERENCES member(member_id)
ON DELETE CASCADE ON UPDATE CASCADE;