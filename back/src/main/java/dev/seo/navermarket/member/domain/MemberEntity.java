package dev.seo.navermarket.member.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/*
 * member_id	int	NO	PRI		auto_increment
 * user_id	varchar(100)	NO	UNI		
 * user_pwd	varchar(255)	NO			
 * email	varchar(100)	NO	UNI		
 * phone	varchar(20)	YES			
 * user_name	varchar(100)	YES			
 * date_of_birth	date	YES			
 * gender	enum('M','F','O')	NO			
 * join_date	timestamp	YES		CURRENT_TIMESTAMP	DEFAULT_GENERATED
 * last_login	timestamp	YES			
 * status	enum('active','inactive','suspended','banned')	YES		active	
 * address	varchar(255)	YES			
 * profile_image	varchar(255)	YES			
 * user_pwd_changed_at	timestamp	YES			
 */

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
	
	@Column(name = "detail_address", nullable = false, length = 255)
	private String detailAddress;
	
	@CreatedDate
	@Column(name = "join_date", updatable = false)
	private LocalDateTime joinDate;
	
	@Column(name = "last_login")
	private LocalDateTime lastLogin;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	@Builder.Default
	private MemberStatus status = MemberStatus.ACTIVE;
	
	@Column(name = "profile_image", length = 255)
	private String profileImage;
	
	@Column(name = "user_pwd_changed_at")
	private LocalDateTime userPwdChangedAt;
	
	@Builder
    public MemberEntity(Long memberId, String userId, String userPwd, String email, String phone, String userName,
                        LocalDate dateOfBirth, Gender gender, String address, String detailAddress, MemberStatus status,
                        LocalDateTime lastLogin, String profileImage, LocalDateTime userPwdChangedAt) {
		this.memberId = memberId;
        this.userId = userId;
        this.userPwd = userPwd;
        this.email = email;
        this.phone = phone;
        this.userName = userName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.address = address;
        this.detailAddress = detailAddress;
        this.status = MemberStatus.SUSPENDED;
        this.lastLogin = lastLogin;
        this.profileImage = profileImage;
        this.userPwdChangedAt = userPwdChangedAt;
	}
	
	@PrePersist
    public void prePersist() {
        if (this.profileImage == null || this.profileImage.isEmpty()) {
            this.profileImage = "default_profile.png"; // 기본 프로필 이미지 URL **만들어야 함**
        }
        if (this.userPwdChangedAt == null) {
            this.userPwdChangedAt = LocalDateTime.now();
        }
    }
	
	// 회원 정보 업데이트
	public void updateMember(String email, String phone, String userName, String nickname, String address, String detailAddress) {
        this.email = email;
        this.phone = phone;
        this.userName = userName;
        this.nickname = nickname;
        this.address = address;
        this.detailAddress = detailAddress;
    }

    public void setStatus(MemberStatus status) {
        this.status = status;
    }

    // 비밀번호 변경 시 사용
    public void updateUserPwd(String newPwd) {
        this.userPwd = newPwd;
        this.userPwdChangedAt = LocalDateTime.now();
    }
}
