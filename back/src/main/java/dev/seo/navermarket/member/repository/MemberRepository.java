package dev.seo.navermarket.member.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.seo.navermarket.member.domain.MemberEntity;

/**
 * @file MemberRepository.java
 * @brief 'member' 테이블에 대한 데이터베이스 접근을 담당하는 JPA 레포지토리 인터페이스입니다.
 * JpaRepository를 상속받아 기본적인 CRUD 기능을 제공하며,
 * 추가적인 쿼리 메서드를 정의하여 특정 조건의 회원을 조회할 수 있습니다.
 */
@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long>	{
	
	// 회원 가입 시 아이디, 이메일 중복이 있다면 해당 메서드 호출
	boolean existsByUserId(String userId);
	boolean existsByEmail(String email);
	boolean existsByNickname(String nickname);
	
	// --- 회원 조회 (관리자 전용 기능) ---

	/**
     * @brief 사용자 로그인 ID(userId)로 회원 엔티티를 조회합니다.
     * @param userId 조회할 사용자 로그인 ID
     * @return Optional<MemberEntity> 해당 사용자 ID를 가진 회원 엔티티 (존재하지 않을 경우 Optional.empty())
     */
	Optional<MemberEntity> findByUserId(String userId);
	
	/**
	 * @brief 사용자 닉네임(nickname)이 포함된 회원 엔티티 리스트를 조회합니다.
	 * @param nickname 조회할 사용자 닉네임 (부분 일치 검색)
	 * @return List<MemberEntity> 해당 닉네임이 포함된 회원 엔티티 리스트
	 */
	List<MemberEntity> findByNicknameContaining(String nickName);
	
	/**
     * @brief 이메일(email)로 회원 엔티티를 조회합니다.
     * @param email 조회할 이메일 주소
     * @return Optional<MemberEntity> 해당 이메일을 가진 회원 엔티티 (존재하지 않을 경우 Optional.empty())
     */
    Optional<MemberEntity> findByEmail(String email);
    
    /**
     * @brief 특정 상태의 회원 엔티티 리스트를 조회합니다. (예: ACTIVE, SUSPENDED 등)
     * @param status 조회할 회원 상태
     * @return List<MemberEntity> 해당 상태의 회원 엔티티 리스트
     */
    List<MemberEntity> findByStatus(dev.seo.navermarket.member.domain.MemberStatus status); // MemberEntity의 Status Enum 사용
    
    // --- 회원 아이디/비밀번호 찾기 기능 (회원 전용 기능) ---
    /**
     * @brief 사용자 이름(userName)과 전화번호(phoneNumber)로 회원 엔티티를 조회합니다.
     * 아이디 찾기 기능에 활용될 수 있습니다.
     * @param userName 조회할 사용자 이름
     * @param phoneNumber 조회할 전화번호
     * @return Optional<MemberEntity> 해당 이름과 전화번호를 가진 회원 엔티티 (존재하지 않을 경우 Optional.empty())
     */
    Optional<MemberEntity> findByUserNameAndPhone(String userName, String phone);
}