package dev.seo.navermarket.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.seo.navermarket.dto.LoginRequestDto;
import dev.seo.navermarket.dto.LoginResponseDto;
import dev.seo.navermarket.dto.SignupRequestDto;
import dev.seo.navermarket.dto.SignupResponseDto;
import dev.seo.navermarket.jwt.JwtTokenProvider;
import dev.seo.navermarket.member.domain.MemberEntity;
import dev.seo.navermarket.repository.MemberRepository;
import dev.seo.navermarket.service.AuthService;
import dev.seo.navermarket.service.MemberService;
import lombok.RequiredArgsConstructor;

/**
 * @file AuthServiceImpl.java
 * @brief AuthService 인터페이스의 구현체 클래스입니다.
 * 인증 관련 비즈니스 로직의 실제 구현을 담당하며, MemberRepository를 통해 사용자 정보를 조회합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션 설정
public class AuthServiceImpl implements AuthService {
	
	private final MemberRepository memberRepository;
	private final MemberService memberService;
	private final JwtTokenProvider jwtTokenProvider;
	
	private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

	/**
     * @brief 사용자 회원가입을 처리합니다.
     * 실제 회원가입 로직은 MemberService에 위임합니다.
     * @param signupRequestDto 회원가입 요청 정보
     * @return SignupResponseDto 회원가입 성공 시 반환되는 정보 (사용자 이름, 메시지 등)
     * @throws RuntimeException 회원가입 실패 시 (예: 아이디 중복, 유효성 검사 실패)
     */
	@Override
	@Transactional // 회원 가입은 쓰기 작업이므로 트랜잭션 활성화
	public SignupResponseDto signup(SignupRequestDto signupRequestDto) {
        log.info("AuthService: 회원가입 요청 수신 - userId: {}", signupRequestDto.getUserId());

        // 실제 회원가입 로직은 MemberService에 위임
        Long newMemberId = memberService.signup(signupRequestDto); // MemberService에서 회원 ID를 반환받음

        // 회원가입 성공 응답 DTO 생성
        String welcomeMessage = String.format("회원 가입에 성공하였습니다. 반갑습니다 %s(%s)님! 이웃과 함께 따듯한 마음을 나눠보아요!", signupRequestDto.getUserId(), signupRequestDto.getUserName());
        return SignupResponseDto.builder()
                .message(welcomeMessage)
                .userName(signupRequestDto.getUserName()) // 요청 DTO에서 사용자 이름 가져옴
                .memberId(newMemberId)
                .userId(signupRequestDto.getUserId()) // 요청 DTO에서 사용자 ID 포함
                .build();
    }
	
	/**
	 * @brief 사용자 로그인을 처리합니다.
	 * @param loginRequestDto 로그인 요청 정보 (사용자 ID, 비밀번호)
	 * @return LoginResponseDto 로그인 성공 시 반환되는 정보 (토큰, 회원 ID 등)
	 * @throws RuntimeException 로그인 실패 시 (예: 사용자 없음, 비밀번호 불일치)
	 */
	@Override
	public LoginResponseDto login(LoginRequestDto loginRequestDto) {
		log.info("AuthService: 로그인 요청 수신 - userId: {}", loginRequestDto.getUserId());
		MemberEntity member = memberRepository.findByUserId(loginRequestDto.getUserId())
				.orElseThrow(() -> {
					log.warn("로그인 실패: 사용자를 찾을 수 없습니다. userId={}", loginRequestDto.getUserId());
					return new RuntimeException("사용자를 찾을 수 없습니다.");
				});
		
		// ✨ 실제 비밀번호 암호화 및 검증 로직이 여기에 들어가야 함.
        // 현재는 평문 비교이므로, Spring Security의 PasswordEncoder를 사용하여 검증
        // if (!passwordEncoder.matches(loginRequestDto.getUserPwd(), member.getUserPwd())) {
        //     log.warn("로그인 실패: 비밀번호 불일치. userId={}", loginRequestDto.getUserId());
        //     throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        // }
		if (!member.getUserPwd().equals(loginRequestDto.getUserPwd())) {
			log.warn("로그인 실패: 비밀번호 불일치. userId={}", loginRequestDto.getUserId());
			throw new RuntimeException("비밀번호가 일치하지 않습니다.");
		}
		
		
		// 더미 토큰 대신 실제 JWT 토큰 생성
		String jwtToken = jwtTokenProvider.generateToken(
				member.getMemberId(),
				member.getUserId(),
				member.getUserName()
				);
		log.info("로그인 성공: JWT 토큰 생성 완료. userId={}", member.getUserId());
		
		return LoginResponseDto.builder()
				.token(jwtToken)
				.memberId(member.getMemberId())
				.userId(member.getUserId())
				.userName(member.getUserName())
				.build();
	}
	
}
