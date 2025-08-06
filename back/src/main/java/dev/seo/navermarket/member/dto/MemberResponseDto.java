package dev.seo.navermarket.member.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import dev.seo.navermarket.member.domain.MemberEntity;

/**
 * @file MemberResponseDto.java
 * @brief 마이페이지, 개인 정보 수정 등 사용자의 상세 정보를 조회할 때 사용되는 응답 DTO 클래스입니다.
 * 보안상 민감하거나 사용자에게 직접 노출할 필요 없는 정보(비밀번호, 권한, 내부 상태 등)는 제외됩니다.
 */
@Getter
@Setter
@ToString
@Builder
public class MemberResponseDto {

    private Long memberId;
    private String userId;
    private String userName;
    private String nickname;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String gender;          // 성별 (ENUM 값을 String으로)
    private String zonecode;
    private String address;         // 주소 (시/군/구까지)
    private String addressDetail;   // 상세 주소 (읍/면/동 이하 상세 주소)
    private String preferredTradeArea; // 선호 거래 지역 이름
    private String profileImageUrl;
    private LocalDateTime joinDate;
    private BigDecimal reputationScore;
    private Integer itemsSoldCount;
    private Integer itemsBoughtCount;
    private Boolean isVerifiedUser; // 본인 인증 여부 (true/false)

    /**
     * @brief MemberEntity로부터 MemberResponseDto를 생성하는 팩토리 메서드입니다.
     * @param memberEntity 변환할 MemberEntity 객체
     * @return MemberResponseDto 변환된 DTO 객체
     */
    public static MemberResponseDto fromEntity(MemberEntity memberEntity) {
    	return MemberResponseDto.builder()
                .memberId(memberEntity.getMemberId())
                .userId(memberEntity.getUserId())
                .userName(memberEntity.getUserName())
                .nickname(memberEntity.getNickname())
                .email(memberEntity.getEmail())
                .phone(memberEntity.getPhone())
                .dateOfBirth(memberEntity.getDateOfBirth())
                .gender(memberEntity.getGender().name()) // 직접 name 호출 => enum에 정의된 상수 자체의 String을 반환하는 메서드.
                .zonecode(memberEntity.getZonecode())
                .address(memberEntity.getAddress())
                .addressDetail(memberEntity.getAddressDetail())
                .preferredTradeArea(memberEntity.getPreferredTradeArea())
                .profileImageUrl(memberEntity.getProfileImageUrl())
                .joinDate(memberEntity.getJoinDate())
                .reputationScore(memberEntity.getReputationScore())
                .itemsSoldCount(memberEntity.getItemsSoldCount())
                .itemsBoughtCount(memberEntity.getItemsBoughtCount())
                .isVerifiedUser(memberEntity.getIsVerifiedUser())
                .build();
    }
    
}