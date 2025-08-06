package dev.seo.navermarket.member.service.impl;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.seo.navermarket.member.domain.MemberEntity;
import dev.seo.navermarket.member.dto.ChangePasswordRequestDto;
import dev.seo.navermarket.member.dto.SignupRequestDto;
import dev.seo.navermarket.member.exception.DuplicateEmailException;
import dev.seo.navermarket.member.exception.DuplicateNicknameException;
import dev.seo.navermarket.member.exception.DuplicateUserIdException;
import dev.seo.navermarket.member.exception.InvalidPasswordException;
import dev.seo.navermarket.member.exception.MemberNotFoundException;
import dev.seo.navermarket.member.exception.PasswordMismatchException;
import dev.seo.navermarket.member.repository.MemberRepository;
import dev.seo.navermarket.member.service.MemberService;
import dev.seo.navermarket.member.service.UserPwdAlertService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor // 생성자 주입을 위해 Lombok 사용
@Transactional(readOnly = true)
@Slf4j
public class MemberServiceImpl implements MemberService {
	
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserPwdAlertService userPwdAlertService;

	// 아이디 중복 확인 로직
	@Override
	public boolean checkUserIdDuplication(String userId) {
		return memberRepository.existsByUserId(userId);
	}
	
	// 닉네임 중복 확인 로직
	@Override
	public boolean checkNicknameDuplication(String nickname) {
		return memberRepository.existsByNickname(nickname);
	}
	
	// 이메일 중복 확인 로직
	@Override
	public boolean checkEmailDuplication(String email) {
		return memberRepository.existsByEmail(email);
	}
	
	/**
     * @brief 사용자 회원가입을 처리합니다.
     * SignupRequestDto를 받아 MemberEntity로 변환하고 저장합니다.
     * @param signupRequestDto 회원가입 요청 데이터
     * @return Long 새로 생성된 회원의 memberId
     * @throws DuplicateUserIdException 아이디 중복 시
     * @throws DuplicateNicknameException 닉네임 중복 시
     * @throws DuplicateEmailException 이메일 중복 시
     */
	@Transactional // 회원 가입은 쓰기 작업이므로 트랜잭션 활성화
	@Override
	public Long signup(SignupRequestDto signupRequestDto) {		
		// --- 1. 중복 확인 (MemberService에서 담당) ---
		// 예외 메시지를 직접 전달하는 대신, ErrorCode에 정의된 메시지를 사용하도록 변경
		if (memberRepository.existsByUserId(signupRequestDto.getUserId())) {
			log.warn("회원 가입 실패: 이미 사용 중인 아이디 입니다. userId={}", signupRequestDto.getUserId());
			throw new DuplicateUserIdException(); // 메시지 없이 던지면 ErrorCode의 메시지 사용
		}
		if (memberRepository.existsByNickname(signupRequestDto.getNickname())) {
			log.warn("회원 가입 실패: 이미 사용 중인 닉네임 입니다. nickname={}", signupRequestDto.getNickname());
			throw new DuplicateNicknameException(); // 메시지 없이 던지면 ErrorCode의 메시지 사용
		}
		if (memberRepository.existsByEmail(signupRequestDto.getEmail())) {
			log.warn("회원 가입 실패: 이미 사용 중인 이메일 입니다. email={}", signupRequestDto.getEmail());
			throw new DuplicateEmailException(); // 메시지 없이 던지면 ErrorCode의 메시지 사용
		}
		
		// --- 2. DTO => Entity 변환 ---
		MemberEntity member = signupRequestDto.toEntity();
		
		// --- 3. 비밀번호 암호화 및 변경 시점 기록
		String encodedPwd = passwordEncoder.encode(member.getUserPwd());
		member.setUserPwd(encodedPwd);
		member.setUserPwdChangedAt(LocalDateTime.now());
		
		// --- 4. 회원 정보 저장 ---
		log.info("회원 정보를 저장합니다: {}", member.getUserId());
		MemberEntity savedMember = memberRepository.save(member);
		log.info("회원 가입이 성공적으로 완료되었습니다: memberId={}, userId={}", savedMember.getMemberId(), savedMember.getUserId());
		
		return savedMember.getMemberId();
	}
	
	/**
     * @brief 사용자의 비밀번호를 변경합니다.
     * @param memberId 비밀번호를 변경할 회원의 ID
     * @param requestDto 비밀번호 변경 요청 데이터 (현재 비밀번호, 새 비밀번호, 새 비밀번호 확인)
     * @throws MemberNotFoundException 회원을 찾을 수 없을 때
     * @throws InvalidPasswordException 현재 비밀번호가 일치하지 않을 때
     * @throws PasswordMismatchException 새 비밀번호와 확인 비밀번호가 일치하지 않을 때
     */
	@Transactional
	@Override
	public void changeUserPwd(Long memberId, ChangePasswordRequestDto requestDto) {
		// 회원을 찾을 수 없을 경우 MemberNotFoundException 발생
		MemberEntity memberEntity = memberRepository.findById(memberId)
								.orElseThrow(MemberNotFoundException::new); // MemberNotFoundException 사용
		
		log.info("회원의 비밀번호 변경 시도: memberId={}", memberId);
		
		// --- 1. 현재 비밀번호 확인 ---
		if (!passwordEncoder.matches(requestDto.getCurrentPassword(), memberEntity.getUserPwd())) {
			log.warn("비밀번호 변경 실패: 현재 비밀번호 불일치. memberId={}", memberId);
			throw new InvalidPasswordException();
		}

		// --- 2. 새 비밀번호와 확인 비밀번호 일치 여부 확인 ---
		if (!requestDto.getNewPassword().equals(requestDto.getConfirmNewPassword())) {
			log.warn("비밀번호 변경 실패: 새 비밀번호와 확인 비밀번호 불일치. memberId={}", memberId);
			throw new PasswordMismatchException(); // PasswordMismatchException 사용
		}

		// 비밀번호 변경 후 (비밀번호 변경 시점 저장)
		String encodedPwd = passwordEncoder.encode(requestDto.getNewPassword());
		memberEntity.setUserPwd(encodedPwd);
		memberEntity.setUserPwdChangedAt(LocalDateTime.now());
		
		// JPA 변경 감지가 자동 적용되므로 save() 호출은 선택 사항이지만 명시적으로 저장하는 것도 좋습니다.
		memberRepository.save(memberEntity);
		log.info("회원의 비밀번호가 성공적으로 변경되었습니다: memberId={}", memberId);
	}
	
	// 비밀번호 변경 알림 로직
	@Override
	public boolean isUserPwdExpired(Long memberId) {
		// JPA의 EntityNotFoundException은 Spring Data JPA가 제공하는 예외입니다.
		// 이를 CustomException으로 래핑하여 던질 수도 있지만, 여기서는 기존 방식을 유지했습니다.
		// GlobalExceptionHandler에서 EntityNotFoundException을 적절히 처리하고 있다면 문제 없습니다.
		MemberEntity memberEntity = memberRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
		return userPwdAlertService.isUserPwdExpired(memberEntity.getUserPwdChangedAt());
	}
	
}
