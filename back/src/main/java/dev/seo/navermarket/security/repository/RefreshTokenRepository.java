package dev.seo.navermarket.security.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.seo.navermarket.security.domain.RefreshTokenEntity;

/**
 * @file RefreshTokenRepository.java
 * @brief RefreshTokenEntity에 대한 데이터베이스 접근을 위한 리포지토리 인터페이스입니다.
 * Spring Data JPA를 사용하여 기본적인 CRUD 및 쿼리 메서드를 제공합니다.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

	/**
     * @brief 주어진 토큰 값으로 RefreshTokenEntity를 조회합니다.
     * @param tokenValue 조회할 리프레시 토큰의 실제 값
     * @return 조회된 RefreshTokenEntity (Optional)
     */
    Optional<RefreshTokenEntity> findByTokenValue(String tokenValue);

    /**
     * @brief 주어진 회원 ID와 토큰 값으로 RefreshTokenEntity를 조회합니다.
     * @param memberId 조회할 회원의 고유 ID
     * @param tokenValue 조회할 리프레시 토큰의 실제 값
     * @return 조회된 RefreshTokenEntity (Optional)
     */
    Optional<RefreshTokenEntity> findByMemberIdAndTokenValue(Long memberId, String tokenValue);

    /**
     * @brief 주어진 회원 ID에 해당하는 모든 리프레시 토큰을 삭제합니다.
     * 주로 로그아웃 시 해당 회원의 모든 토큰을 무효화할 때 사용됩니다.
     * @param memberId 삭제할 회원의 고유 ID
     */
    void deleteByMemberId(Long memberId);
    
}
