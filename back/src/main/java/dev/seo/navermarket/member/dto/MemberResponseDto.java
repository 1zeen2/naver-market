package dev.seo.navermarket.member.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class MemberResponseDto {
	
	private Long memberId; // 회원 고유 번호
    private String userId; // 로그인 ID (마이페이지 등에서 관리 용도로 사용)
    private String userName; // 실제 이름 (본인 인증 용도, 프론트 노출 최소화)
    private String nickname; // 사용자의 닉네임
    private String profileImageUrl; // 프로필 사진 URL (선택 사항, 기본 이미지 경로 가능)
    private int reputationScore; // 매너 온도/평판 점수 (기본값 0 또는 36.5 등)
    private int itemsSoldCount; // 판매 완료 상품 수
    private int itemsBoughtCount; // 구매 완료 상품 수
    private String areaName; // 현재 설정된 동네/지역 이름 (예: "서초동")
    private boolean isVerifiedUser; // 본인 인증 여부 (true/false)
    private String email; // 이메일 (필요하다면 추가)

}