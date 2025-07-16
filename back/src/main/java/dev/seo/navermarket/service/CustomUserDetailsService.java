package dev.seo.navermarket.service;

import dev.seo.navermarket.member.domain.MemberEntity;
import dev.seo.navermarket.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User; // Spring Security User 객체 임포트
import org.springframework.security.core.userdetails.UserDetails; // UserDetails 인터페이스 임포트
import org.springframework.security.core.userdetails.UserDetailsService; // UserDetailsService 인터페이스 임포트
import org.springframework.security.core.userdetails.UsernameNotFoundException; // UsernameNotFoundException 임포트
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 트랜잭션 임포트

import java.util.ArrayList; // 빈 권한 리스트 생성을 위한 임포트

/**
 * @file CustomUserDetailsService.java
 * @brief Spring Security의 UserDetailsService 인터페이스를 구현하여 사용자 정보를 로드합니다.
 * JWT 토큰에서 추출된 사용자 ID를 기반으로 데이터베이스에서 사용자(MemberEntity)를 조회하고,
 * 이를 Spring Security의 UserDetails 객체로 변환하여 인증 과정에서 사용될 수 있도록 합니다.
 */
@Service // Spring 빈으로 등록
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성하여 의존성 주입
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션 설정
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final MemberRepository memberRepository; // MemberEntity 조회를 위한 리포지토리 주입

    /**
     * @brief 사용자 ID(여기서는 userId)를 기반으로 사용자 정보를 로드합니다.
     * 이 메서드는 Spring Security의 인증 과정에서 호출됩니다.
     *
     * @param userId 로드할 사용자의 로그인 ID
     * @return UserDetails Spring Security가 사용하는 사용자 정보 객체
     * @throws UsernameNotFoundException 해당 사용자 ID를 가진 사용자를 찾을 수 없을 때 발생
     */
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        log.debug("UserDetailsService: 사용자 ID '{}'로 사용자 정보 로드 시도", userId);

        // MemberRepository를 사용하여 데이터베이스에서 MemberEntity를 조회합니다.
        // 사용자를 찾을 수 없으면 UsernameNotFoundException을 발생시킵니다.
        MemberEntity member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("UserDetailsService: 사용자 ID '{}'를 찾을 수 없습니다.", userId);
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId);
                });

        log.info("UserDetailsService: 사용자 ID '{}'의 정보 로드 성공.", userId);

        // 조회된 MemberEntity 정보를 Spring Security의 UserDetails 객체로 변환하여 반환합니다.
        // UserDetails 객체는 Spring Security가 인증 및 권한 부여를 위해 사용하는 표준 인터페이스입니다.
        // 여기서는 `User` 클래스(UserDetails의 구현체)를 사용합니다.
        //
        // 매개변수:
        // 1. username: 사용자의 고유 식별자 (여기서는 userId)
        // 2. password: 사용자의 비밀번호 (BCrypt로 인코딩된 비밀번호)
        // 3. authorities: 사용자가 가진 권한 목록 (현재는 빈 리스트로 설정. 실제 권한은 MemberEntity에 추가 후 설정)
        return new User(member.getUserId(), member.getUserPwd(), new ArrayList<>()); // ✨ 현재는 빈 권한 리스트
    }
}