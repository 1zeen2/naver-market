package dev.seo.navermarket.member.restcontroller;

import java.util.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
	
	// 아이디 중복 확인 API EndPoint: GET 요청으로 쿼리 파라미터를 받음
    @GetMapping("/check-id")
    public ResponseEntity<Map<String, Object>> checkUserId(@RequestParam String userId) {
    	Map<String, Object> response = new HashMap<>();
    	
        if (userId == null || userId.trim().isEmpty()) {
        	log.warn("아이디 입력 후 중복 확인을 해주세요.");
            response.put("isAvailable", false);
            response.put("message", "아이디는 필수 항목입니다.");
            return ResponseEntity.badRequest().body(response);
        }
        
        boolean isDuplicate = memberService.checkUserIdDuplication(userId);

        response.put("isAvailable", !isDuplicate);
        if (!isDuplicate) {
        	response.put("message", "사용 가능한 아이디 입니다.");
        } else {
        	response.put("message", "이미 사용 중인 아이디 입니다.");
        }
        log.info("아이디 중복 확인 '{}': isAvailable={}", userId, !isDuplicate);
        
        return ResponseEntity.ok(response);
    }
    
    // 이메일 중복 확인 API EndPoint
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();

        if (email == null || email.trim().isEmpty()) {
            log.warn("이메일 입력 후 중복 확인을 해주세요");
            response.put("isAvailable", false);
            response.put("message", "이메일은 필수 항목입니다.");
            return ResponseEntity.badRequest().body(response);
        }

        boolean isDuplicate = memberService.checkEmailDuplication(email);

        response.put("isAvailable", !isDuplicate);
        if (!isDuplicate) {
            response.put("message", "사용 가능한 이메일입니다.");
        } else {
            response.put("message", "이미 가입되어있는 이메일입니다.");
        }
        log.info("이메일 중복 확인 '{}': isAvailable={}", email, !isDuplicate);

        return ResponseEntity.ok(response);
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
