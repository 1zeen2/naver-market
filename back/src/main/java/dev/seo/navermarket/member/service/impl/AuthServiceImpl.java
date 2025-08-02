package dev.seo.navermarket.member.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.seo.navermarket.member.domain.MemberEntity;
import dev.seo.navermarket.member.dto.LoginRequestDto;
import dev.seo.navermarket.member.dto.LoginResponseDto;
import dev.seo.navermarket.member.dto.SignupRequestDto;
import dev.seo.navermarket.member.dto.SignupResponseDto;
import dev.seo.navermarket.member.repository.MemberRepository;
import dev.seo.navermarket.member.service.AuthService;
import dev.seo.navermarket.member.service.MemberService;
import dev.seo.navermarket.security.jwt.JwtTokenProvider;
import dev.seo.navermarket.common.exception.InvalidPasswordException;
import dev.seo.navermarket.common.exception.InvalidTokenException;
import dev.seo.navermarket.common.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @file AuthServiceImpl.java
 * @brief AuthService 인터페이스의 구현체 클래스입니다.
 * 인증 관련 비즈니스 로직의 실제 구현을 담당하며, MemberRepository를 통해 사용자 정보를 조회합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션 설정
@Slf4j
public class AuthServiceImpl implements AuthService {
	
	private final MemberRepository memberRepository;
	private final MemberService memberService;
	private final JwtTokenProvider jwtTokenProvider;
	private final PasswordEncoder passwordEncoder;

	/**
     * @brief 사용자 회원가입을 처리합니다.
     * 실제 회원가입 로직은 MemberService에 위임합니다.
     * @param signupRequestDto 회원가입 요청 정보
     * @return SignupResponseDto 회원가입 성공 시 반환되는 정보 (사용자 이름, 메시지 등)
     * @throws dev.seo.navermarket.common.exception.DuplicateUserIdException 아이디 중복 시
     * @throws dev.seo.navermarket.common.exception.DuplicateEmailException 이메일 중복 시
     * @throws IllegalArgumentException 유효성 검사 실패 시 (MemberService에서 발생)
     */
	@Override
	@Transactional // 회원 가입은 쓰기 작업이므로 트랜잭션 활성화
	public SignupResponseDto signup(SignupRequestDto signupRequestDto) {
        log.info("AuthService: 회원가입 요청 수신 - userId: {}", signupRequestDto.getUserId());

        // MemberService에서 중복 검사 및 회원가입 로직을 수행하고 예외를 던지도록 수정
        // MemberService.signup 메서드에서 DuplicateUserIdException, DuplicateEmailException을 던지도록 변경해야 합니다.
        Long newMemberId = memberService.signup(signupRequestDto);

        String welcomeMessage = String.format("회원 가입에 성공하였습니다. 반갑습니다 %s님! 이웃과 함께 따듯한 마음을 나눠보아요!", signupRequestDto.getNickname());
        return SignupResponseDto.builder()
                .message(welcomeMessage)
                .memberId(newMemberId)
                .userId(signupRequestDto.getUserId())
                .nickname(signupRequestDto.getNickname())
                .build();
    }
	
	/**
     * @brief 사용자 로그인을 처리합니다.
     * @param loginRequestDto 로그인 요청 정보 (사용자 ID, 비밀번호)
     * @return LoginResponseDto 로그인 성공 시 반환되는 정보 (토큰, 회원 ID 등)
     * @throws UserNotFoundException 사용자를 찾을 수 없을 때
     * @throws InvalidPasswordException 비밀번호가 일치하지 않을 때
     */
	@Override
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        log.info("AuthService: 로그인 요청 수신 - userId: {}", loginRequestDto.getUserId());
        MemberEntity member = memberRepository.findByUserId(loginRequestDto.getUserId())
                .orElseThrow(() -> {
                    log.warn("로그인 실패: 사용자를 찾을 수 없습니다. userId={}", loginRequestDto.getUserId());
                    return new UserNotFoundException("사용자를 찾을 수 없습니다."); // 커스텀 예외 사용
                });

        if (!passwordEncoder.matches(loginRequestDto.getUserPwd(), member.getUserPwd())) {
            log.warn("로그인 실패: 비밀번호 불일치. userId={}", loginRequestDto.getUserId());
            throw new InvalidPasswordException("비밀번호가 일치하지 않습니다.");
        }

        String jwtToken = jwtTokenProvider.generateToken(
                member.getMemberId(),
                member.getUserId(),
                member.getUserName(),
                member.getRole().name()
        );
        log.info("로그인 성공: JWT 토큰 생성 완료. userId={}", member.getUserId());

        return LoginResponseDto.builder()
                .token(jwtToken)
                .memberId(member.getMemberId())
                .userId(member.getUserId())
                .userName(member.getUserName())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .preferredTradeArea(member.getPreferredTradeArea())
                .reputationScore(member.getReputationScore())
                .sellingInProgressCount(member.getItemsSoldCount())
                .buyingInProgressCount(member.getItemsBoughtCount())
                .unreadMessageCount(0)
                .build();
    }
	
	/**
     * @brief JWT 토큰에서 사용자 정보를 추출하여 LoginResponseDto 형태로 반환합니다.
     * 주로 토큰을 통해 사용자 정보를 조회하는 API (/api/auth/me)에서 사용됩니다.
     * @param token 유효한 JWT 토큰 문자열
     * @return LoginResponseDto 토큰에서 추출된 사용자 정보 및 DB에서 조회된 최신 정보
     * @throws InvalidTokenException 토큰이 유효하지 않거나 만료되었을 때
     * @throws UserNotFoundException 토큰의 memberId로 사용자를 찾을 수 없을 때
     */
    @Override
    @Transactional(readOnly = true)
    public LoginResponseDto getAuthenticatedUserInfo(String token) {
        // 1. JWT 토큰 유효성 검사
        if (!jwtTokenProvider.validateToken(token)) {
            log.warn("인증된 사용자 정보 조회 실패: 유효하지 않거나 만료된 JWT 토큰입니다.");
            throw new InvalidTokenException("토큰이 유효하지 않거나 만료되었습니다."); // <-- throw로 변경
        }

        // 2. 토큰에서 memberId 추출
        Long memberId = jwtTokenProvider.extractMemberId(token);
        if (memberId == null) {
            log.warn("인증된 사용자 정보 조회 실패: 토큰에 memberId 클레임이 없습니다.");
            throw new InvalidTokenException("토큰에 사용자 식별 정보가 없습니다."); // <-- throw로 변경
        }

        // 3. memberId를 통해 DB에서 최신 MemberEntity 정보 조회
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.error("인증된 사용자 정보 조회 실패: DB에서 memberId={}인 사용자를 찾을 수 없습니다. (데이터 불일치 가능성)", memberId);
                    return new UserNotFoundException("사용자 정보를 찾을 수 없습니다."); // 커스텀 예외 사용
                });

        // TODO: sellingInProgressCount, buyingInProgressCount, unreadMessageCount는 실제 로직으로 대체 필요
        return LoginResponseDto.builder()
                .token(token)
                .memberId(member.getMemberId())
                .userId(member.getUserId())
                .userName(member.getUserName())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .preferredTradeArea(member.getPreferredTradeArea())
                .reputationScore(member.getReputationScore())
                .sellingInProgressCount(member.getItemsSoldCount()) // 임시
                .buyingInProgressCount(member.getItemsBoughtCount()) // 임시
                .unreadMessageCount(0) // 임시
                .build();
    }
    
}