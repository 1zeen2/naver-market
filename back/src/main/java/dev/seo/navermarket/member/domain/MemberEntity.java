package dev.seo.navermarket.member.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "member")
// @Data는 equals/hashCode에 문제가 생길 수 있다고 하여 사용하는 어노테이션을 직접 사용
@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(AuditingEntityListener.class)
public class MemberEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "user_id", nullable = false, unique = true, length = 100)
    private String userId;

    @Column(name = "user_pwd", nullable = false, length = 255)
    private String userPwd;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "address_detail", nullable = false, length = 255)
    private String addressDetail;

    @CreatedDate
    @Column(name = "join_date", updatable = false)
    private LocalDateTime joinDate;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default // Lombok @Builder를 사용하여 기본값을 설정할 때 사용합니다.
    private MemberStatus status = MemberStatus.ACTIVE;

    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl;

    @Column(name = "user_pwd_changed_at")
    private LocalDateTime userPwdChangedAt;

    @Column(name = "reputation_score", nullable = false, precision = 4, scale = 1)
    @Builder.Default
    private Double reputationScore = 36.5; // 기본값 36.5

    @Column(name = "items_sold_count", nullable = false)
    @Builder.Default
    private Integer itemsSoldCount = 0;

    @Column(name = "items_bought_count", nullable = false)
    @Builder.Default
    private Integer itemsBoughtCount = 0;

    @Column(name = "is_verified_user", nullable = false)
    @Builder.Default
    private Boolean isVerifiedUser = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;
    
    @Column(name = "preferred_trade_area")
    private String preferredTradeArea;
	
	@PrePersist
    public void prePersist() {
        if (this.profileImageUrl == null || this.profileImageUrl.isEmpty()) {
            this.profileImageUrl = "default_profile.png"; // 기본 프로필 이미지 URL **만들어야 함**
        }
        if (this.userPwdChangedAt == null) {
            this.userPwdChangedAt = LocalDateTime.now();
        }
    }
	
	// 회원 정보 업데이트
	public void updateMember(String email, String phone, String userName, String nickname, String address, String addressDetail) {
        this.email = email;
        this.phone = phone;
        this.userName = userName;
        this.nickname = nickname;
        this.address = address;
        this.addressDetail = addressDetail;
    }

    // 비밀번호 변경 시 사용
    public void updateUserPwd(String newPwd) {
        this.userPwd = newPwd;
        this.userPwdChangedAt = LocalDateTime.now();
    }

    // 매너 온도 업데이트 메서드 추가
    public void updateReputationScore(Double changeAmount) {
        if (this.reputationScore == null) {
            this.reputationScore = 0.0; // 초기화
        }
        this.reputationScore += changeAmount;
        // 매너 온도 상한/하한 설정 (예: 0.0 ~ 100.0)
        this.reputationScore = Math.max(0.0, Math.min(100.0, this.reputationScore));
    }

    // 판매/구매 카운트 증가 메서드 추가
    public void incrementItemsSoldCount() {
        this.itemsSoldCount++;
    }

    public void incrementItemsBoughtCount() {
        this.itemsBoughtCount++;
    }

    // 본인 인증 상태 변경 메서드 추가
    public void setVerifiedUser(Boolean isVerifiedUser) {
        this.isVerifiedUser = isVerifiedUser;
    }

    // 역할 변경 메서드 추가 (관리자 기능)
    public void setRole(Role role) {
        this.role = role;
    }
}