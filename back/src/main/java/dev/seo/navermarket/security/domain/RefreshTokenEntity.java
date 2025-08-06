package dev.seo.navermarket.security.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @file RefreshTokenEntity.java
 * @brief 리프레시 토큰 정보를 저장하는 엔티티입니다.
 * 사용자 ID, 토큰 값, 만료 시간, 그리고 토큰의 유효성(폐기 여부)을 관리합니다.
 */
@Entity
@Table(name = "refresh_token") // 테이블명은 refresh_token으로 설정
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 사용을 위한 기본 생성자
@AllArgsConstructor // 모든 필드를 포함하는 생성자
@Builder // 빌더 패턴 사용
public class RefreshTokenEntity {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long tokenId; // 리프레시 토큰의 고유 ID

    @Column(name = "member_id", nullable = false)
    private Long memberId; // 토큰을 발급받은 회원의 고유 ID

    @Column(name = "token_value", nullable = false, unique = true, length = 500)
    private String tokenValue; // 리프레시 토큰의 실제 값 (JWT)

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate; // 리프레시 토큰의 만료 시간

    @Column(name = "revoked", nullable = false)
    private boolean revoked; // 토큰 폐기 여부 (로그아웃, 재발급 시 이전 토큰 폐기)

    /**
     * @brief 리프레시 토큰을 폐기 상태로 변경합니다.
     */
    public void revoke() {
        this.revoked = true;
    }
}
