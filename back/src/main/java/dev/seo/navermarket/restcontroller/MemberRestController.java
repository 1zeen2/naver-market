package dev.seo.navermarket.restcontroller;

import java.util.*;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.seo.navermarket.dto.SignupRequestDTO;
import dev.seo.navermarket.entity.MemberEntity;
import dev.seo.navermarket.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor // MemberService 주입을 위해 Lombok 사용
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class MemberRestController {
	
	private static final Logger log = LoggerFactory.getLogger(MemberRestController.class);
	
	private final MemberService memberService;
	
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
	
	// 아이디 중복 확인 API EndPoint: GET 요청으로 쿼리 파라미터를 받음
    @GetMapping("/member/check-id")
    public ResponseEntity<Map<String, Boolean>> checkUserId(@RequestParam("userId") String userId) {
    	Map<String, Boolean> response = new HashMap<>();
    	
        if (userId == null || userId.trim().isEmpty()) {
        	log.warn("아이디 입력 후 중복 확인을 해주세요.");
            response.put("isAvailable", false);
            return ResponseEntity.badRequest().body(response);
        }
        
        if (userId.trim().length() < 3 || !userId.matches("^[a-zA-Z0-9]+$")) {
        	log.warn("아이디는 3글자 이상의 영문, 숫자만 가능합니다.");
        	response.put("isAvailable", false);
        	return ResponseEntity.badRequest().body(response);
        }

        boolean isDuplicate = memberService.checkUserIdDuplication(userId);

        response.put("isAvailable", !isDuplicate); // isDuplicate가 true면 아이디 중복으로 해당 아이디 가입 불가
        log.info("아이디 중복 확인 '{}': isAvailable={}", userId, !isDuplicate);
        
        return ResponseEntity.ok(response);
    }
    
    // 이메일 중복 확인 API EndPotin
    @GetMapping("/member/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam("email") String email) {
    	Map<String, Boolean> response = new HashMap<>();
    	
    	if (email == null || email.trim().isEmpty()) {
    		log.warn("이메일 입력 후 중복 확인을 해주세요");
    		response.put("isAvailable", false);
    		return ResponseEntity.badRequest().body(response);
    	}
    	
    	if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
    		log.warn("유효하지 않은 이메일 형식입니다: {}", email);
            response.put("isAvailable", false);
            return ResponseEntity.badRequest().body(response);
    	}
    	
    	boolean isDuplicate = memberService.checkEmailDuplication(email);
    	
    	response.put("isAvliable", !isDuplicate);
    	log.info("이메일 중복 확인 '{}': isAvailavble={}", email, !isDuplicate);
    	
    	return ResponseEntity.ok(response);
    	
    }

	
	// 회원 가입 API EndPoint
	@PostMapping("/signup")
	public ResponseEntity<String> signup(@Valid @RequestBody SignupRequestDTO signupRequestDTO) {
		// @Valid 어노테이션이 붙어 있으면, SignupRequestDTO의 유효성 검사가 자동으로 수행됩니다.
		// 만약 유효성 검사에 실패하면, MethodArgumentNotValidException이 발생합니다.
		
		// 1. Controller에서 받은 signupRequestDTO의 내용 확인
		log.info("받은 SignupRequestDTO: {}", signupRequestDTO);
		
		// signupRequestDTO.toEntity() 호출 전에, DTO의 내용이 올바른지 확인하는 것이 중요합니다.
        // 만약 DTO 자체가 null이거나 DTO 필드들이 null이면, toEntity() 결과도 null이 될 수 있습니다.
        if (signupRequestDTO == null) {
            log.error("SignupRequestDTO가 비어있음. 클라이언트의 요청 데이터를 확인하세요..");
            return ResponseEntity.badRequest().body("회원 가입 요청 데이터가 비어있습니다.");
        }
		
        // ✨ 2. signupRequestDTO.toEntity()가 반환하는 MemberEntity 객체를 'memberEntity' 변수에 저장
        // 이 변수를 사용하여 로그를 찍고, null 검사를 수행하며, Service로 전달해야 합니다.
        MemberEntity memberEntity = signupRequestDTO.toEntity();
        
        // 3. toEntity() 메서드가 변환한 Member 엔티티 객체의 내용 확인
        // 이 엔티티 객체는 데이터베이스에 저장될 최종 형태이므로, 이 단계에서 데이터가 비어있다면
        // toEntity() 메서드에 문제가 있거나, signupRequestDTO 자체가 비어있는 것입니다.
        log.info("저장하기 전에 변환된 MemberEntity: {}", memberEntity);

        if (memberEntity == null) {
             log.error("변환된 Member Entity가 비어있음. SignupRequestDTO.toEntity() 메서드를 확인할 것.");
             return ResponseEntity.internalServerError().body("회원 엔티티 변환에 실패했습니다.");
        }
        
        // MemberService의 signUp 메서드는 유효성 검사를 가지고 있음
        // Service에서 예외 처리를 하면 여기서 catch
        try {
			memberService.signUp(memberEntity);
			return ResponseEntity.status(HttpStatus.CREATED).body("회원 가입이 성공적으로 완료되었습니다."); // 201 Created
        } catch (RuntimeException e) {
        	log.error("회원 가입 실패: {}", e.getMessage());
        	return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // 409 Conflict
        } catch (Exception e) {
        	log.error("회원 가입 중 예상치 못한 에러가 발생했습니다: {}", e.getMessage(), e);
        	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 가입 중 알 수 없는 오류가 발생했습니다.");
        }
	}
	
	// 비밀번호 변경
	@PutMapping("/member/{memberId}/userPwd")
	public ResponseEntity<String> updateUserPwd(@PathVariable Long memberId, @RequestParam("newUserPwd") String newUserPwd) {
		// 비밀번호는 로그에 직접 찍으면 안됨
		log.info("회원의 비밀번호 변경 시도: {} 새로운 비밀번호 (masked): [PROTECTED]", memberId);
		
		if (newUserPwd == null || newUserPwd.trim().isEmpty() || newUserPwd.trim().length() < 8) {
            return ResponseEntity.badRequest().body("새 비밀번호는 8자리 이상이어야 합니다.");
        }
		
		memberService.changeUserPwd(memberId, newUserPwd);
		return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
	}
	
	// 비밀번호 변경 기간 로직
	@GetMapping("/member/{memberId}/userPwdExpired")
	public ResponseEntity<Boolean> checkUserPwdExpired(@PathVariable Long memberId) {
		log.info("회원의 비밀번호 만료 기간: {}", memberId);
		
		if (memberId == null || memberId <= 0) {
			return ResponseEntity.badRequest().body(false);
		}
		
		boolean expired = memberService.isUserPwdExpired(memberId);
		return ResponseEntity.ok(expired);
	}

}
