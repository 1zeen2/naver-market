package dev.seo.navermarket.service;

import dev.seo.navermarket.entity.MemberEntity;

public interface MemberService {
	void signUp(MemberEntity member);
	void changeUserPwd(Long memberId, String newUserPwd);
	boolean isUserPwdExpired(Long memberId);
	boolean checkUserIdDuplication(String userId);
	boolean checkEmailDuplication(String email);
}
