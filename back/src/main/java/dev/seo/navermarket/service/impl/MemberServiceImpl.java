package dev.seo.navermarket.service.impl;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.seo.navermarket.dto.SignupRequestDto;
import dev.seo.navermarket.member.domain.MemberEntity;
import dev.seo.navermarket.repository.MemberRepository;
import dev.seo.navermarket.service.MemberService;
import dev.seo.navermarket.service.UserPwdAlertService;
import dev.seo.navermarket.validator.SignUpValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // MemberRepository와 PasswordEncoder 주입을 위해 Lombok 사용
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {
	
	private static final Logger log = LoggerFactory.getLogger(MemberServiceImpl.class);
	private final MemberRepository memberRepository;
	private final SignUpValidator signupValidator;
	private final PasswordEncoder passwordEncoder;
	private final UserPwdAlertService userPwdAlertService;

	// 아이디 중복 확인 로직
	@Override
	public boolean checkUserIdDuplication(String userId) {
		return memberRepository.existsByUserId(userId);
	}
	
	// 이메일 중복 확인 로직
	@Override
	@Transactional(readOnly = true)
	public boolean checkEmailDuplication(String email) {
		log.debug("이메일 중복 확인 로그.디버그: {}", email);
		return memberRepository.existsByEmail(email);
	}
	
	/**
     * @brief 사용자 회원가입을 처리합니다.
     * SignupRequestDto를 받아 MemberEntity로 변환하고 저장합니다.
     * @param signupRequestDto 회원가입 요청 데이터
     * @return Long 새로 생성된 회원의 memberId
     */
	@Transactional // 회원 가입은 쓰기 작업이므로 트랜잭션 활성화
	@Override
	public Long signup(SignupRequestDto signupRequestDto) {
		MemberEntity member = signupRequestDto.toEntity();
		
		if (memberRepository.existsByUserId(member.getUserId())) {
			throw new RuntimeException("이미 가입되어있는 아이디 입니다.");
		}
		if (memberRepository.existsByEmail(member.getEmail())) {
			throw new RuntimeException("이미 가입되어있는 이메일 입니다.");
		}
		signupValidator.validateSignUp(member); // 추가 유효성 검사
		
		String encodedPwd = passwordEncoder.encode(member.getUserPwd());
		member.setUserPwd(encodedPwd);
		member.setUserPwdChangedAt(LocalDateTime.now());
		
		log.info("회원 정보를 저장합니다: {}", member.getUserId());
		MemberEntity savedMember = memberRepository.save(member);
		log.info("회원 가입이 성공적으로 완료되었습니다: {}", savedMember.getUserId());
		
		return savedMember.getMemberId();
	}
	
	// 비밀번호 변경 로직
	@Transactional
	@Override
	public void changeUserPwd(Long memberId, String newUserPwd) {
		MemberEntity memEntity = memberRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
		
		// 비밀번호 변경 전 로깅
        log.info("Changing password for memberId: {} with new password (encoded): {}", memberId, passwordEncoder.encode(newUserPwd));

		// 비밀번호 변경 후 비밀번호 변경 시점 저장
		String encodedPwd = passwordEncoder.encode(newUserPwd);
		memEntity.setUserPwd(encodedPwd);
		memEntity.setUserPwdChangedAt(LocalDateTime.now());
		
		// JPA 변경 감지가 자동 적용되므로 생략 가능
		memberRepository.save(memEntity);
		log.info("회원의 비밀번호가 성공적으로 변경되었습니다: {}", memberId);
	}
	
	// 비밀번호 변경 알림 로직
	@Override
	public boolean isUserPwdExpired(Long memberId) {
		MemberEntity memEntity = memberRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
		return userPwdAlertService.isUserPwdExpired(memEntity.getUserPwdChangedAt());
	}
	
<<<<<<< HEAD
}
=======
}
>>>>>>> 4d505b2aff6a49a7d6bd4034d08618b54258b019
