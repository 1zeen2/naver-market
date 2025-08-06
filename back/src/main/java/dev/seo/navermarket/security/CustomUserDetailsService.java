package dev.seo.navermarket.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.seo.navermarket.member.domain.MemberEntity;
import dev.seo.navermarket.member.repository.MemberRepository;

import java.util.Collections;
import java.util.List;

/**
 * Spring Security의 UserDetailsService 인터페이스를 구현하여 사용자 정보를 로드합니다.
 * 데이터베이스에서 사용자(MemberEntity)를 조회하고, 이를 UserDetails 객체로 변환하여 반환합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository; // MemberEntity를 조회하기 위한 레포지토리 주입

    /**
     * 사용자명(여기서는 사용자 ID)을 기반으로 사용자 정보를 로드합니다.
     *
     * @param username 사용자의 ID (이메일 또는 고유 ID)
     * @return UserDetails 객체 (Spring Security의 User 객체)
     * @throws UsernameNotFoundException 해당 사용자명을 가진 사용자가 없을 경우 발생
     */
    @Override
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 설정하여 성능 최적화
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Attempting to load user by username: {}", username);

        // 1. 데이터베이스에서 사용자 ID(username)로 MemberEntity 조회
        // findByUserId를 사용하여 사용자 ID로 검색합니다.
        return memberRepository.findByUserId(username)
                .map(this::createUserDetails) // 조회된 MemberEntity를 UserDetails로 변환
                .orElseThrow(() -> {
                    log.warn("User not found with username: {}", username);
                    return new UsernameNotFoundException(username + " -> 데이터베이스에서 찾을 수 없습니다.");
                });
    }

    /**
     * MemberEntity 객체를 Spring Security의 UserDetails 객체로 변환합니다.
     * UserDetails는 Spring Security가 인증 및 권한 부여를 위해 사용하는 사용자 정보 인터페이스입니다.
     *
     * @param memberEntity 조회된 MemberEntity 객체
     * @return Spring Security의 UserDetails (User) 객체
     */
    private UserDetails createUserDetails(MemberEntity memberEntity) {
        log.debug("Creating UserDetails for user: {}", memberEntity.getUserId());

        // MemberEntity의 권한(role) 정보를 GrantedAuthority 리스트로 변환
        // 현재는 MemberEntity에 role 필드가 없으므로, 기본적으로 "ROLE_USER" 권한을 부여합니다.
        // 실제 애플리케이션에서는 MemberEntity에 role 필드를 추가하고 해당 정보를 사용해야 합니다.
        List<GrantedAuthority> grantedAuthorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        // 만약 MemberEntity에 Set<Role> roles 필드가 있다면 다음과 같이 변환할 수 있습니다.
        // List<GrantedAuthority> grantedAuthorities = memberEntity.getRoles().stream()
        //         .map(role -> new SimpleGrantedAuthority(role.getName()))
        //         .collect(Collectors.toList());

        // Spring Security의 User 객체를 생성하여 반환
        // User 객체의 첫 번째 인자는 사용자명(userId), 두 번째 인자는 암호화된 비밀번호, 세 번째 인자는 권한 목록입니다.
        return new User(
                memberEntity.getUserId(), // 사용자 ID
                memberEntity.getUserPwd(), // 암호화된 비밀번호
                grantedAuthorities // 사용자 권한
        );
    }
}
