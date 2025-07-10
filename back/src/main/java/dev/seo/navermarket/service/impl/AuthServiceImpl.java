package dev.seo.navermarket.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.seo.navermarket.dto.LoginRequestDto;
import dev.seo.navermarket.dto.LoginResponseDto;
import dev.seo.navermarket.member.domain.MemberEntity;
import dev.seo.navermarket.repository.MemberRepository;
import dev.seo.navermarket.service.AuthService;
import lombok.RequiredArgsConstructor;

/**
 * @file AuthServiceImpl.java
 * @brief AuthService 인터페이스의 구현체 클래스입니다.
 * 인증 관련 비즈니스 로직의 실제 구현을 담당하며, MemberRepository를 통해 사용자 정보를 조회합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {
	
	private final MemberRepository memberRepository;

	/**
     * @brief 사용자 로그인을 처리합니다.
     * @param loginRequestDto 로그인 요청 정보 (사용자 ID, 비밀번호)
     * @return LoginResponseDto 로그인 성공 시 반환되는 정보 (토큰, 회원 ID 등)
     * @throws RuntimeException 로그인 실패 시 (예: 사용자 없음, 비밀번호 불일치)
     */
	@Override
	public LoginResponseDto login(LoginRequestDto loginRequestDto) {
		MemberEntity member = memberRepository.findByUserId(loginRequestDto.getUserId())
				.orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
		
		if (!member.getUserPwd().equals(loginRequestDto.getUserPwd())) {
	        throw new RuntimeException("비밀번호가 일치하지 않습니다.");
	    }
		
		String dummyToken = "dummy_jwt_token_for_" + member.getUserId();
		
		return LoginResponseDto.builder()
                .token(dummyToken)
                .memberId(member.getMemberId())
                .userId(member.getUserId())
                .userName(member.getUserName())
                .build();
	}
	
}
