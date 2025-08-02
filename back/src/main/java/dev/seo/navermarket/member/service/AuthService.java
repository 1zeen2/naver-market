package dev.seo.navermarket.member.service;

import dev.seo.navermarket.member.dto.LoginRequestDto;
import dev.seo.navermarket.member.dto.LoginResponseDto;
import dev.seo.navermarket.member.dto.SignupRequestDto;
import dev.seo.navermarket.member.dto.SignupResponseDto;

/**
 * @file AuthService.java
 * @brief 인증(로그인, 회원가입 등) 관련 비즈니스 로직을 정의하는 서비스 인터페이스입니다.
 * 실제 구현은 AuthServiceImpl 클래스에서 이루어집니다.
 */
public interface AuthService {
	
	/**
     * @brief 사용자 회원가입을 처리합니다.
     * 이 메서드는 실제 회원가입 로직을 MemberService에 위임합니다.
     * @param signupRequestDto 회원가입 요청 정보
     * @return SignupResponseDto 회원가입 성공 시 반환되는 정보 (사용자 이름, 메시지 등)
     * @throws RuntimeException 회원가입 실패 시 (예: 아이디 중복, 유효성 검사 실패)
     */
    SignupResponseDto signup(SignupRequestDto signupRequestDto);
	
	/**
     * @brief 사용자 로그인을 처리합니다.
     * @param loginRequestDto 로그인 요청 정보 (사용자 ID, 비밀번호)
     * @return LoginResponseDto 로그인 성공 시 반환되는 정보 (토큰, 회원 ID 등)
     * @throws RuntimeException 로그인 실패 시 (예: 사용자 없음, 비밀번호 불일치)
     */
	LoginResponseDto login(LoginRequestDto loginRequestDto);

    /**
     * @brief JWT 토큰에서 사용자 정보를 추출하여 LoginResponseDto 형태로 반환합니다.
     * @param token 유효한 JWT 토큰 문자열
     * @return LoginResponseDto 토큰에서 추출된 사용자 정보 및 DB에서 조회된 최신 정보
     * @throws dev.seo.navermarket.common.exception.InvalidTokenException 토큰이 유효하지 않거나 만료되었을 때
     * @throws dev.seo.navermarket.common.exception.UserNotFoundException 토큰의 memberId로 사용자를 찾을 수 없을 때
     */
    LoginResponseDto getAuthenticatedUserInfo(String token);
    
}