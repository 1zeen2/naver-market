package dev.seo.navermarket.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.seo.navermarket.entity.MemberEntity;

@Repository
public interface MemberRepositoryDAO extends JpaRepository<MemberEntity, Long>	{
	
	// 회원 가입 시 아이디, 이메일 중복이 있다면 해당 메서드 호출
	boolean existsByUserId(String userId);
	boolean existsByEmail(String email);
	
	// 회원 이름으로 회원 정보 찾기 기능
	Optional<MemberEntity> findByUserId(String userId);	
}