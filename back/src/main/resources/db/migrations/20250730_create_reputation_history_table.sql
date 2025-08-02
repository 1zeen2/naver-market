USE naver_market;

-- `reputation_history` 테이블 생성 (컬럼 타입 일관성 유지 버전)
CREATE TABLE reputation_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL COMMENT '점수가 변경된 회원 ID',
    change_type VARCHAR(50) NOT NULL COMMENT '변경 유형 (예: DEAL_COMPLETED, DEAL_FAILED, REPORTED)',
    change_amount DECIMAL(4,1) NOT NULL COMMENT '점수 변화량 (양수:증가, 음수:감소)',
    current_score_at_change DECIMAL(4,1) NOT NULL COMMENT '변경 후 최종 점수',
    related_entity_type VARCHAR(50) NULL COMMENT '관련 엔티티 유형 (예: PRODUCT, REPORT)',
    related_entity_id BIGINT NULL COMMENT '관련 엔티티 ID',
    reason VARCHAR(255) NULL COMMENT '변경 사유 설명',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '기록 생성 시각',
    INDEX idx_member_id_created_at (member_id, created_at),
    FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE CASCADE
) COMMENT '회원 매너 온도 변경 이력';