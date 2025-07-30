package dev.seo.navermarket.member.service;

import dev.seo.navermarket.member.dto.SignupRequestDto;

public interface MemberService {

	// 아이디 중복 확인 로직
	boolean checkUserIdDuplication(String userId);
	
	// 이메일 중복 확인 로직
	boolean checkEmailDuplication(String email);
	
	/**
     * @brief 사용자 회원가입을 처리합니다.
     * @param signupRequestDto 회원가입 요청 데이터
     * @return Long 새로 생성된 회원의 memberId
     */
	Long signup(SignupRequestDto signupRequestDto);
	
	// 비밀번호 변경 로직
	void changeUserPwd(Long memberId, String newUserPwd);
	
	// 비밀번호 유효 기간 로직
	boolean isUserPwdExpired(Long memberId);
}
