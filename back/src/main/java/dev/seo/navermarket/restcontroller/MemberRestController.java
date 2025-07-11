package dev.seo.navermarket.restcontroller;

import java.util.*;
import java.util.regex.Pattern;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.seo.navermarket.service.MemberService;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor // MemberService 주입을 위해 Lombok 사용
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class MemberRestController {
	
	private static final Logger log = LoggerFactory.getLogger(MemberRestController.class);
	
	private final MemberService memberService;
	
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
	
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
        
        if (userId.trim().length() < 3 || !userId.matches("^[a-zA-Z0-9]+$")) {
        	log.warn("아이디는 3글자 이상의 영문, 숫자만 가능합니다.");
        	response.put("isAvailable", false);
        	response.put("message", "아이디는 3글자 이상의 영문, 숫자만 가능합니다.");
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

        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            log.warn("유효하지 않은 이메일 형식입니다: {}", email);
            response.put("isAvailable", false);
            response.put("message", "유효하지 않은 이메일 형식입니다.");
            return ResponseEntity.badRequest().body(response);
        }

        boolean isDuplicate = memberService.checkEmailDuplication(email);

        response.put("isAvailable", !isDuplicate);
        if (!isDuplicate) { // 이메일이 사용 가능한 경우 => isAvailable == true
            response.put("message", "사용 가능한 이메일입니다.");
        } else { // 이메일이 이미 사용 중인 경우 => isAvailable == false
            response.put("message", "이미 가입되어있는 이메일입니다.");
        }
        log.info("이메일 중복 확인 '{}': isAvailable={}", email, !isDuplicate);

        return ResponseEntity.ok(response);
    }
	
	// 비밀번호 변경
	@PutMapping("/{memberId}/userPwd")
	public ResponseEntity<String> updateUserPwd(@PathVariable Long memberId, @RequestParam String newUserPwd) {
		// 비밀번호는 로그에 직접 찍으면 안됨
		log.info("회원의 비밀번호 변경 시도: {} 새로운 비밀번호 (masked): [PROTECTED]", memberId);
		
		if (newUserPwd == null || newUserPwd.trim().isEmpty() || newUserPwd.trim().length() < 8) {
            return ResponseEntity.badRequest().body("비밀번호는 8자리 이상이어야 합니다.");
        }
		
		memberService.changeUserPwd(memberId, newUserPwd);
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
