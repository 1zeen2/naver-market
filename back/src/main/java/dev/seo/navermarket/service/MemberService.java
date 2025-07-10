package dev.seo.navermarket.service;

import dev.seo.navermarket.member.domain.MemberEntity;

public interface MemberService {
	void signup(MemberEntity member);
	void changeUserPwd(Long memberId, String newUserPwd);
	boolean isUserPwdExpired(Long memberId);
	boolean checkUserIdDuplication(String userId);
	boolean checkEmailDuplication(String email);
}
