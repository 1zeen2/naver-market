package dev.seo.navermarket.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import dev.seo.navermarket.member.domain.MemberEntity;
import lombok.Getter;

/**
 * @file CustomUserDetails.java
 * @brief Spring Security의 UserDetails 인터페이스를 구현하여
 * 인증된 사용자의 상세 정보를 담는 클래스입니다.
 * JWT 토큰에서 사용자 정보를 추출하여 SecurityContext에 저장될 때 사용됩니다.
 */
@Getter
public class CustomUserDetails implements UserDetails {

    // 직렬화 버전 ID (Serializable 인터페이스 구현 시 권장)
    private static final long serialVersionUID = 1L;

    private Long memberId;
    private String userId;
    private String password; // 회원의 비밀번호 (해시된 비밀번호) - UserDetails 인터페이스 때문에 필요하지만, 실제 값은 보안상 불필요
    private String nickname;
    private Collection<? extends GrantedAuthority> authorities;

    /**
     * @brief MemberEntity로부터 CustomUserDetails를 생성하는 생성자입니다.
     * 주로 CustomUserDetailsService에서 사용됩니다.
     * @param member 인증된 MemberEntity 객체
     */
    public CustomUserDetails(MemberEntity member) {
        this.memberId = member.getMemberId();
        this.userId = member.getUserId();
        this.password = member.getUserPwd(); // 해시된 비밀번호를 저장 (인증 후에는 사용되지 않음)
        this.nickname = member.getNickname();
        // MemberEntity의 실제 역할(Role)을 기반으로 권한 설정
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority(member.getRole().name()));
    }
	
    /**
     * @brief JWT 클레임으로부터 CustomUserDetails를 재구성하는 생성자입니다.
     * 주로 JwtTokenProvider에서 JWT 토큰을 파싱하여 Authentication 객체를 만들 때 사용됩니다.
     * @param memberId 회원의 고유 ID
     * @param userId 회원의 로그인 ID
     * @param password 비밀번호 (JWT에 포함되지 않으므로 빈 문자열 또는 null로 설정)
     * @param nickname 회원의 닉네임
     * @param authorities 회원의 권한 목록
     */
    public CustomUserDetails(Long memberId, String userId, String password, String nickname, Collection<? extends GrantedAuthority> authorities) {
        this.memberId = memberId;
        this.userId = userId;
        this.password = password;
        this.nickname = nickname;
        this.authorities = authorities;
    }
    
	// --- UserDetails 인터페이스 구현 메서드 ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() { // UserDetails의 getUsername()은 로그인 ID를 반환
        return userId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 여부 (만료되지 않음) - 실제 구현 시 MemberEntity의 상태를 확인
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠금 여부 (잠금되지 않음) - 실제 구현 시 MemberEntity의 상태를 확인
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 자격 증명 만료 여부 (만료되지 않음) - 실제 구현 시 비밀번호 변경일 등을 확인
    }

    @Override
    public boolean isEnabled() {
        // 계정 활성화 여부 (활성화됨) - 실제 구현 시 MemberEntity의 status 필드를 기반으로 활성화 여부 반환 가능
        // 예: return member.getStatus() == MemberStatus.ACTIVE;
        return true;
    }

    // --- 커스텀 메서드 (UserDetails 인터페이스에 포함되지 않은 추가 필드 게터) ---
    public Long getMemberId() {
        return this.memberId;
    }

    public String getNickname() {
        return this.nickname;
    }
    
}