package dev.seo.navermarket.member.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import dev.seo.navermarket.member.domain.Gender;
import dev.seo.navermarket.member.domain.MemberEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SignupRequestDto {
    // @NotBlank: null, 빈 문자열, 공백 문자열만 있는 경우를 모두 허용하지 않음 (String 타입에 사용)
    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    private String userId;
    
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    private String userPwd;
    
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.") // 이메일 형식 검증 추가
    private String email;
    
    @NotBlank(message = "전화번호는 필수 입력 값입니다.")
    private String phone;
    
    @NotBlank(message = "이름은 필수 입력 값입니다.")
    private String userName;
    
    private String nickname;
    
    // @NotNull: null만 허용하지 않음 (객체 타입에 사용, LocalDate는 객체이므로)
    @NotNull(message = "생년월일은 필수 입력 값입니다.")
    @Past(message = "생년월일은 미래일 수 없습니다.") // 생년월일이 과거 날짜인지 검증
    private LocalDate dateOfBirth;
    
    @NotBlank(message = "성별은 필수 입력 값입니다.")
    private String gender; // 문자열로 받아서 Enum으로 변환
    
    @NotBlank(message = "주소는 필수 입력 값입니다.")
    private String address;
    
    @NotBlank(message = "상세 주소는 필수 입력 값입니다.")
    private String detailAddress;
    
    // MemberEntity의 @PrePersist에서 기본값 설정이 처리되므로 DTO에서는 이 필드들을 넘기지 않아도 됩니다.
    public MemberEntity toEntity() {
        return MemberEntity.builder()
                .userId(this.userId)
                .userPwd(this.userPwd)
                .email(this.email)
                .phone(this.phone)
                .userName(this.userName)
                .dateOfBirth(this.dateOfBirth)
                .gender(Gender.valueOf(this.gender.toUpperCase())) // 문자열을 Enum으로 변환
                .address(this.address)
                .detailAddress(this.detailAddress)
                .build();
    }
}