package dev.seo.navermarket.security;

import dev.seo.navermarket.member.domain.MemberEntity;
import dev.seo.navermarket.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
@Slf4j
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
    	MemberEntity member = memberRepository.findByUserId(userId)
    			.orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다:" + userId));
    	
    	// 모든 사용자에게 기본적으로 "ROLE_USER" 권한을 부여.
    	List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    	
    	log.debug("User loaded: userId={}, nickname={}, authorities={}", member.getUserId(), member.getNickname(), authorities);
    	
    	// CustomUserDetails가 Spring Security의 UserDetails 인터페이스를 올바르게 구현하고,
        // 특히 생성자에서 MemberEntity와 권한(authorities)을 받아 처리하는지 확인해야 합니다.
        // 만약 CustomUserDetails 클래스가 권한 설정을 직접 처리하지 못한다면,
        // org.springframework.security.core.userdetails.User 객체를 직접 반환하는 것이 더 명확합니다.

        // 현재는 CustomUserDetails가 있다고 가정하고,
        // CustomUserDetails 내부에서 member 객체를 통해 권한이 올바르게 설정되는지 확인이 필요합니다.
        // 예: CustomUserDetails 생성자가 MemberEntity와 List<GrantedAuthority>를 받는 경우
        // return new CustomUserDetails(member, authorities);

        // 하지만 CustomUserDetails의 코드를 알 수 없으므로,
        // Spring Security의 기본 User 객체를 사용하는 방식으로 대체하여 가장 안전한 방법을 제시합니다.
        // CustomUserDetails가 필요한 경우, 해당 클래스에 권한 부여 로직을 추가해야 합니다.
    	
    	// 조회된 MemberEntity를 CustomUserDetails 객체로 변환하여 반환.
    	return new org.springframework.security.core.userdetails.User(
    			member.getUserId(),
    			member.getUserPwd(),
    			authorities
		);
    }
        
}