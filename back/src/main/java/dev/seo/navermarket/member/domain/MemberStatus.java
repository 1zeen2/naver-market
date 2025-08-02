package dev.seo.navermarket.member.domain;

public enum MemberStatus {
	ACTIVE,		// 활성 계정 (정상)
	INACTIVE,	// 비활성 계정 (장기간 미접속)
	SUSPENDED,	// 차단 전, 일시 정지 계정
	BANNED		// 차단된 회원
}