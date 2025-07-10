USE naver_market;

-- 회원 정보 테이블 생성
CREATE TABLE member (
    member_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '회원 고유 ID',
    user_id VARCHAR(100) NOT NULL UNIQUE COMMENT '사용자 아이디',
    user_pwd VARCHAR(255) NOT NULL COMMENT '비밀번호',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '이메일 주소',
    phone VARCHAR(20) NOT NULL COMMENT '전화번호',
    user_name VARCHAR(100) NOT NULL COMMENT '사용자 이름',
    date_of_birth DATE NOT NULL COMMENT '생년월일',
    gender ENUM('M', 'F', 'O') NOT NULL COMMENT '성별 (M:남성, F:여성, O:기타)',
    address VARCHAR(255) NOT NULL COMMENT '기본 주소',
    detail_address VARCHAR(255) NOT NULL COMMENT '상세 주소',
    join_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '회원 가입일',
    last_login DATETIME NULL DEFAULT NULL COMMENT '마지막 로그인 시점',
    status ENUM('active', 'inactive', 'suspended', 'banned') NOT NULL DEFAULT 'suspended' COMMENT '회원 상태',
    profile_image VARCHAR(255) NULL COMMENT '프로필 이미지 URL',
    user_pwd_changed_at DATETIME NULL COMMENT '비밀번호 변경 시점', -- COMMENT 수정

    -- 인덱스 추가: 검색 성능 향상을 위해 특정 컬럼에 인덱스를 생성합니다.
    -- UNIQUE 제약 조건이 있는 컬럼은 자동으로 UNIQUE INDEX가 생성되지만,
    -- 명시적으로 이름을 지정하거나, 추가적인 비-고유 인덱스가 필요할 때 사용합니다.
    INDEX idx_member_user_id (user_id),
    INDEX idx_member_email (email)
) COMMENT '회원 정보 테이블';