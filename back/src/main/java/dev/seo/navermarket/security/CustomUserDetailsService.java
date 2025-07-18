package dev.seo.navermarket.security;

import dev.seo.navermarket.member.domain.MemberEntity;
import dev.seo.navermarket.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @file CustomUserDetailsService.java
 * @brief Spring Security의 UserDetailsService 인터페이스를 구현하여 사용자 정보를 로드합니다.
 * JWT 토큰에서 추출된 사용자 ID를 기반으로 데이터베이스에서 사용자(MemberEntity)를 조회하고,
 * 이를 Spring Security의 UserDetails 객체로 변환하여 인증 과정에서 사용될 수 있도록 합니다.
 */
@Service // Spring 빈으로 등록
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성하여 의존성 주입
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository; // MemberEntity 조회를 위한 리포지토리 주입

    /**
     * @brief 주어진 사용자 이름(로그인 ID)으로 사용자 상세 정보를 로드합니다.
     * @param userId 사용자의 로그인 ID
     * @return UserDetails 구현체 (CustomUserDetails)
     * @throws UsernameNotFoundException 해당 사용자 ID를 찾을 수 없을 경우
     */
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
    	// memberRepository를 사용하여 userId로 MemberEntity를 조회.
    	// findByUserId 메서드는 MemberRepository에 정의되어 있어야 함.
    	MemberEntity member = memberRepository.findByUserId(userId)
    			.orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다:" + userId));
    	
    	// 조회된 MemberEntity를 CustomUserDetails 객체로 변환하여 반환.
    	return new CustomUserDetails(member);
    }
        
}