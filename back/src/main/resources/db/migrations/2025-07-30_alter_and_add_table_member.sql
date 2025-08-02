USE naver_market;

-- 1. `profile_image` 컬럼을 `profile_image_url`로 이름 변경
ALTER TABLE member
RENAME COLUMN profile_image TO profile_image_url;

-- 2. `detail_address` 컬럼을 `address_detail`로 이름 변경
ALTER TABLE member
RENAME COLUMN detail_address TO address_detail;

-- 3. 새로운 컬럼들 추가
ALTER TABLE member
    ADD COLUMN reputation_score DECIMAL(4,1) NOT NULL DEFAULT 36.5 COMMENT '매너 온도/평판 점수',
    ADD COLUMN items_sold_count INT NOT NULL DEFAULT 0 COMMENT '판매 완료 상품 수',
    ADD COLUMN items_bought_count INT NOT NULL DEFAULT 0 COMMENT '구매 완료 상품 수',
    ADD COLUMN is_verified_user BOOLEAN NOT NULL DEFAULT FALSE COMMENT '본인 인증 여부',
    ADD COLUMN role ENUM('USER', 'ADMIN', 'BOSS') NOT NULL DEFAULT 'USER' COMMENT '회원 역할 (USER, ADMIN, BOSS 등)';

-- 4. 기존 컬럼에 대한 제약 조건 추가/변경 (선택 사항)
-- 닉네임과 이메일은 고유해야 하므로 UNIQUE 제약 조건 추가 고려
ALTER TABLE member
ADD CONSTRAINT UQ_nickname UNIQUE (nickname);