package dev.seo.navermarket.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @file TokenResponseDto.java
 * @brief 로그인 및 토큰 갱신 시 클라이언트에게 반환될 토큰 및 사용자 기본 정보를 담는 DTO입니다.
 * 액세스 토큰과 리프레시 토큰, 그리고 로그인 성공 시 필요한 사용자 식별 정보를 포함합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponseDto {
    private String accessToken; // 발급된 액세스 토큰
    private String refreshToken; // 발급된 리프레시 토큰
    private Long memberId; // 사용자 고유 ID
    private String userId; // 사용자 로그인 ID
    private String nickname; // 사용자 닉네임
    private String profileImageUrl; // 사용자 프로필 이미지 URL
    private String preferredTradeArea; // 선호 거래 지역
    private Double reputationScore; // 평판 점수
    private Integer sellingInProgressCount; // 판매 중인 상품 수 (임시)
    private Integer buyingInProgressCount; // 구매 중인 상품 수 (임시)
    private Integer unreadMessageCount; // 읽지 않은 메시지 수 (임시)
}

