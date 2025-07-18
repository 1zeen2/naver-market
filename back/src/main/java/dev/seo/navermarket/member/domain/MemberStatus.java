package dev.seo.navermarket.member.domain;

public enum MemberStatus {
	ACTIVE,		// 활성 계정 (정상)
	INACTIVE,	// 비활성 계정 (장기간 미접속)
	SUSPENDED,	// 이메일 인증이 되지 않은 회원
	BANNED		// 차단된 회원
}