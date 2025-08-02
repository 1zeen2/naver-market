package dev.seo.navermarket.member.service;

import dev.seo.navermarket.member.dto.ChangePasswordRequestDto;
import dev.seo.navermarket.member.dto.SignupRequestDto;

public interface MemberService {

	// 아이디 중복 확인 로직
	boolean checkUserIdDuplication(String userId);
	
	// 닉네임 중복 확인 로직
	boolean checkNicknameDuplication(String nickname);
	
	// 이메일 중복 확인 로직
	boolean checkEmailDuplication(String email);
	
	/**
     * @brief 사용자 회원가입을 처리합니다.
     * @param signupRequestDto 회원가입 요청 데이터
     * @return Long 새로 생성된 회원의 memberId
     */
	Long signup(SignupRequestDto signupRequestDto);
	
	/**
     * @brief 사용자의 비밀번호를 변경합니다.
     * @param memberId 비밀번호를 변경할 회원의 ID
     * @param requestDto 비밀번호 변경 요청 데이터 (현재 비밀번호, 새 비밀번호, 새 비밀번호 확인)
     */
	void changeUserPwd(Long memberId, ChangePasswordRequestDto requestDto);
	
	// 비밀번호 유효 기간 로직
	boolean isUserPwdExpired(Long memberId);
	
}
