package dev.seo.navermarket.service.impl;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.seo.navermarket.dao.MemberRepositoryDAO;
import dev.seo.navermarket.entity.MemberEntity;
import dev.seo.navermarket.service.MemberService;
import dev.seo.navermarket.service.UserPwdAlertService;
import dev.seo.navermarket.validator.SignUpValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;



@Service
@RequiredArgsConstructor // MemberRepository와 PasswordEncoder 주입을 위해 Lombok 사용
public class MemberServiceImpl implements MemberService {
	
	private static final Logger log = LoggerFactory.getLogger(MemberServiceImpl.class);
	private final MemberRepositoryDAO memberDAO;
	private final SignUpValidator signupValidator;
	private final PasswordEncoder passwordEncoder;
	private final UserPwdAlertService userPwdAlertService;

	// 아이디 중복 확인 로직
	@Override
	public boolean checkUserIdDuplication(String userId) {
		return memberDAO.existsByUserId(userId);
	}
	
	// 이메일 중복 확인 로직
	@Override
	@Transactional(readOnly = true)
	public boolean checkEmailDuplication(String email) {
		log.debug("이메일 중복 확인 로그.디버그: {}", email);
		return memberDAO.existsByEmail(email);
	}
	
	// 회원 가입 로직
	@Transactional
	@Override
	public void signup(MemberEntity member) {
		// 회원 가입 유효성 검사시 이상이 없다면 회원 정보를 DB에 저장
		signupValidator.validateSignUp(member);
		
		// 비밀번호 암호화 후 저장
		String encodedPwd = passwordEncoder.encode(member.getUserPwd());
		member.setUserPwd(encodedPwd);
		member.setUserPwdChangedAt(LocalDateTime.now());
		
		// 엔티티가 데이터베이스에 저장될 준비가 되었는지 최종 확인 로그 추가
        log.info("회원 정보를 저장합니다: {}", member);
        memberDAO.save(member);
		log.info("회원 가입이 성공적으로 완료되었습니다.", member.getUserId());
	}
	
	// 비밀번호 변경 로직
	@Transactional
	@Override
	public void changeUserPwd(Long memberId, String newUserPwd) {
		MemberEntity memEntity = memberDAO.findById(memberId).orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
		
		// 비밀번호 변경 전 로깅
        log.info("Changing password for memberId: {} with new password (encoded): {}", memberId, passwordEncoder.encode(newUserPwd));

		// 비밀번호 변경 후 비밀번호 변경 시점 저장
		String encodedPwd = passwordEncoder.encode(newUserPwd);
		memEntity.setUserPwd(encodedPwd);
		memEntity.setUserPwdChangedAt(LocalDateTime.now());
		
		// JPA 변경 감지가 자동 적용되므로 생략 가능
		memberDAO.save(memEntity);
		log.info("회원의 비밀번호가 성공적으로 변경되었습니다: {}", memberId);
	}
	
	// 비밀번호 변경 알림 로직
	@Override
	public boolean isUserPwdExpired(Long memberId) {
		MemberEntity memEntity = memberDAO.findById(memberId).orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
		return userPwdAlertService.isUserPwdExpired(memEntity.getUserPwdChangedAt());
	}
	
}
