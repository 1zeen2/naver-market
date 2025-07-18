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
	
	private Long memberId;
	private String userId;
	private String password; // 회원의 비밀번호 (해시된 비밀번호)
	private Collection<? extends GrantedAuthority> authorities;

	/**
     * @brief MemberEntity로부터 CustomUserDetails를 생성하는 생성자입니다.
     * @param member 인증된 MemberEntity 객체
     */
	public CustomUserDetails(MemberEntity member) {
		this.memberId = member.getMemberId();
		this.userId = member.getUserId();
		this.password = member.getUserPwd();
		this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
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

    // --- 커스텀 메서드 ---
    /**
     * @brief 회원의 고유 ID(PK)를 반환합니다.
     * @return 회원의 고유 ID
     */
    public Long getMemberId() {
        return this.memberId;
    }
}
