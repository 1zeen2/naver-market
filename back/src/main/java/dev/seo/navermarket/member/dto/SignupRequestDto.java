package dev.seo.navermarket.member.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import dev.seo.navermarket.member.domain.Gender;
import dev.seo.navermarket.member.domain.MemberEntity;
import dev.seo.navermarket.validator.ValidPassword;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SignupRequestDto {
	
    // @NotBlank: null, 빈 문자열, 공백 문자열만 있는 경우를 모두 허용하지 않음 (String 타입에 사용)
	@NotBlank(message = "아이디는 필수 입력 항목입니다.")
    @Size(min = 3, max = 20, message = "아이디는 3자 이상 20자 이하로 입력해주세요.")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문과 숫자만 가능합니다.")
    private String userId;
    
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @ValidPassword
    private String userPwd;
    
    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    @Size(min = 2, max = 8, message = "이름은 2자 이상 8자 이하로 입력해주세요.")
    private String userName;
    
    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(min = 2, max = 16, message = "닉네임은 2자 이상 16자 이하로 입력해주세요.")
    private String nickname;
    
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;
    
    @NotBlank(message = "전화번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$", message = "유효한 전화번호 형식이 아닙니다.") // 전화번호 패턴 추가 (예시)
    private String phone;
    
    @NotNull(message = "생년월일은 필수 입력 항목입니다.")
    @Past(message = "생년월일은 미래일 수 없습니다.")
    private LocalDate dateOfBirth;

    @NotBlank(message = "성별은 필수 입력 항목입니다.")
    private String gender;

    @NotBlank(message = "우편번호는 필수 입력 항목입니다.")
    private String zonecode;
    
    @NotBlank(message = "주소는 필수 입력 항목입니다.")
    private String address;

    @NotBlank(message = "상세 주소는 필수 입력 항목입니다.")
    private String addressDetail;
    
    @NotBlank(message = "거래 선호 지역은 필수 입력 항목입니다.")
    private String preferredTradeArea;
    
    public MemberEntity toEntity() {
        return MemberEntity.builder()
                .userId(this.userId)
                .userPwd(this.userPwd)
                .userName(this.userName)
                .nickname(this.nickname)
                .email(this.email)
                .phone(this.phone)
                .dateOfBirth(this.dateOfBirth)
                .gender(Gender.valueOf(this.gender.toUpperCase())) // 문자열을 Enum으로 변환
                .zonecode(this.zonecode)
                .address(this.address)
                .addressDetail(this.addressDetail)
                .preferredTradeArea(this.preferredTradeArea)
                .build();
    }
    
}