package dev.seo.navermarket.member.restcontroller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.seo.navermarket.common.dto.CheckResponseDto;
import dev.seo.navermarket.member.dto.ChangePasswordRequestDto;
import dev.seo.navermarket.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor // MemberService 주입을 위해 Lombok 사용
//@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@Slf4j
public class MemberRestController {
	
	private final MemberService memberService;
	
	/**
     * @brief 아이디, 닉네임, 이메일 중복 확인 로직의 공통 부분을 처리하는 헬퍼 메서드입니다.
     * @param value 확인할 값 (아이디, 닉네임, 이메일)
     * @param type 값의 종류 (예: "아이디", "닉네임", "이메일")
     * @param duplicationCheckFn 중복 확인을 수행할 서비스 메서드 (예: memberService::checkUserIdDuplication)
     * @return CheckResponseDto 중복 확인 결과 DTO
     */
	private ResponseEntity<CheckResponseDto> handleDuplicationCheck(String value, String type, java.util.function.Function<String, Boolean> duplicationCheckFn) {
		if (value == null || value.trim().isEmpty()) {
			log.warn("{} 입력 후 중복 확인을 해주세요", type);
			return ResponseEntity.badRequest().body(CheckResponseDto.builder()
					.isAvailable(false)
					.message(type + "는 필수 입력 항목입니다.")
					.build());
		}
		
		boolean isDuplicate = duplicationCheckFn.apply(value);
		
		String message;
		
		if (!isDuplicate) {
			message = "사용 가능한 " + type + " 입니다.";
		} else {
			message = "이미 사용 중인 " + type + " 입니다.";
		}
		log.info("{} 중복 확인 '{}': isAvailable={}", type, value, !isDuplicate);
		
		return ResponseEntity.ok(CheckResponseDto.builder()
				.isAvailable(isDuplicate)
				.message(message)
				.build());
	}
	
	// 아이디 중복 확인 API EndPoint
    @GetMapping("/check-id")
    public ResponseEntity<CheckResponseDto> checkUserId(@RequestParam String userId) {
        return handleDuplicationCheck(userId, "아이디", memberService::checkUserIdDuplication);
    }
    
    // 이메일 중복 확인 API EndPoint
    @GetMapping("/check-email")
    public ResponseEntity<CheckResponseDto> checkEmail(@RequestParam String email) {
        return handleDuplicationCheck(email, "이메일", memberService::checkEmailDuplication);
    }
    
 // 닉네임 중복 확인 Api EndPoint
    @GetMapping("/check-nickname")
    public ResponseEntity<CheckResponseDto> checkNickname(@RequestParam String nickname) {
        return handleDuplicationCheck(nickname, "닉네임", memberService::checkNicknameDuplication);
    }
	
    /**
     * @brief 사용자의 비밀번호를 변경하는 API 엔드포인트입니다.
     * PUT /api/member/{memberId}/userPwd
     * @param memberId 비밀번호를 변경할 회원의 ID
     * @param requestDto 비밀번호 변경 요청 데이터 (현재 비밀번호, 새 비밀번호, 새 비밀번호 확인)
     * @return ResponseEntity<String> 비밀번호 변경 성공 메시지와 HTTP 상태 코드 (200 OK)
     */
	@PutMapping("/{memberId}/userPwd")
	public ResponseEntity<String> updateUserPwd(@PathVariable Long memberId, 
												@Valid @RequestBody ChangePasswordRequestDto requestDto) {
		// 비밀번호는 로그에 직접 찍으면 안됨
		log.info("회원의 비밀번호 변경 시도: memberId={}", memberId);
		
		memberService.changeUserPwd(memberId, requestDto);
		return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
	}
	
	// 비밀번호 변경 기간 로직
	@GetMapping("/{memberId}/userPwdExpired")
	public ResponseEntity<Boolean> checkUserPwdExpired(@PathVariable Long memberId) {
		log.info("회원의 비밀번호 만료 기간: {}", memberId);
		
		if (memberId == null || memberId <= 0) {
			return ResponseEntity.badRequest().body(false);
		}
		
		boolean expired = memberService.isUserPwdExpired(memberId);
		return ResponseEntity.ok(expired);
	}

}
