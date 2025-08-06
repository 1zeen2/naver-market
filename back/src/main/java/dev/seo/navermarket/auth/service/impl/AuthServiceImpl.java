package dev.seo.navermarket.auth.service.impl;

import dev.seo.navermarket.auth.dto.LoginRequestDto;
import dev.seo.navermarket.auth.service.AuthService;
import dev.seo.navermarket.exception.CustomException;
import dev.seo.navermarket.exception.ErrorCode;
import dev.seo.navermarket.member.domain.MemberEntity;
import dev.seo.navermarket.member.dto.ChangePasswordRequestDto;
import dev.seo.navermarket.member.dto.LoginResponseDto;
import dev.seo.navermarket.member.dto.ResetPasswordRequestDto;
import dev.seo.navermarket.member.dto.SignupRequestDto;
import dev.seo.navermarket.member.dto.SignupResponseDto;
import dev.seo.navermarket.member.exception.MemberNotFoundException;
import dev.seo.navermarket.member.exception.InvalidPasswordException;
import dev.seo.navermarket.member.exception.PasswordMismatchException;
import dev.seo.navermarket.member.repository.MemberRepository;
import dev.seo.navermarket.member.service.MemberService;
import dev.seo.navermarket.security.domain.RefreshTokenEntity;
import dev.seo.navermarket.security.dto.TokenResponseDto;
import dev.seo.navermarket.security.jwt.JwtTokenProvider;
import dev.seo.navermarket.security.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Collection;

/**
 * @brief 사용자 인증(회원가입, 로그인) 관련 비즈니스 로직을 처리하는 서비스 구현체입니다.
 * MemberRepository, PasswordEncoder, JwtTokenProvider, AuthenticationManager를 사용하여
 * 사용자 등록 및 인증, JWT 토큰 발급을 수행합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final MemberService memberService;
    private final RefreshTokenRepository refreshTokenRepository; // RefreshTokenRepository 주입

    @Override
    @Transactional
    public SignupResponseDto signup(SignupRequestDto signupRequestDto) {
        log.info("회원가입 요청: 사용자 ID = {}", signupRequestDto.getUserId());

        Long memberId = memberService.signup(signupRequestDto);

        return SignupResponseDto.builder()
                .memberId(memberId)
                .userId(signupRequestDto.getUserId())
                .message("회원가입이 성공적으로 완료되었습니다.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResponseDto login(LoginRequestDto loginRequestDto) {
        log.info("로그인 요청: 사용자 ID = {}", loginRequestDto.getUserId());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequestDto.getUserId(), loginRequestDto.getUserPwd())
            );

            log.info("로그인 성공: 회원 ID = {}", authentication.getName());

            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

            // TODO: 로그인 시 기존 리프레시 토큰 처리 (예: 이전 토큰 폐기 또는 재발급 정책에 따라)
            // 여기서는 단순히 새로운 리프레시 토큰을 저장합니다.

            MemberEntity member = memberRepository.findByUserId(authentication.getName())
                    .orElseThrow(MemberNotFoundException::new);

            RefreshTokenEntity newRefreshToken = RefreshTokenEntity.builder()
                    .memberId(member.getMemberId())
                    .tokenValue(refreshToken)
                    .expiryDate(LocalDateTime.now().plus(Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpirationMillis())))
                    .revoked(false)
                    .build();
            refreshTokenRepository.save(newRefreshToken);


            return TokenResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .memberId(member.getMemberId())
                    .userId(member.getUserId())
                    .nickname(member.getNickname())
                    .profileImageUrl(member.getProfileImageUrl())
                    .preferredTradeArea(member.getPreferredTradeArea())
                    // .sellingInProgressCount(member.getSellingInProgressCount()) // MemberEntity에 해당 메서드 없음
                    // .buyingInProgressCount(member.getBuyingInProgressCount()) // MemberEntity에 해당 메서드 없음
                    // .unreadMessageCount(member.getUnreadMessageCount()) // MemberEntity에 해당 메서드 없음
                    .build();
        } catch (AuthenticationException e) {
            log.warn("로그인 실패: 사용자 ID = {}, 원인 = {}", loginRequestDto.getUserId(), e.getMessage());
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    @Override
    @Transactional // 리프레시 토큰 갱신은 DB 쓰기 작업이 포함되므로 @Transactional
    public TokenResponseDto refreshTokens(String refreshToken) {
        log.info("리프레시 토큰 갱신 요청: {}", refreshToken);

        // 1. 리프레시 토큰 유효성 검사 (JWT 형식 및 서명)
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("리프레시 토큰 갱신 실패: 유효하지 않은 리프레시 토큰입니다.");
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 2. 리프레시 토큰에서 사용자 ID 추출
        String userId = jwtTokenProvider.extractUserId(refreshToken);

        // 3. DB에서 사용자 정보 조회
        MemberEntity member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("리프레시 토큰 갱신 실패: 사용자 ID {}에 해당하는 회원을 찾을 수 없습니다.", userId);
                    return new MemberNotFoundException();
                });

        // 4. DB에 저장된 리프레시 토큰 조회 및 유효성 확인
        RefreshTokenEntity storedRefreshToken = refreshTokenRepository.findByTokenValue(refreshToken)
                .orElseThrow(() -> {
                    log.warn("리프레시 토큰 갱신 실패: DB에서 리프레시 토큰을 찾을 수 없습니다. tokenValue={}", refreshToken);
                    throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
                });

        // 5. 저장된 토큰이 이미 폐기되었는지 확인
        if (storedRefreshToken.isRevoked()) {
            log.warn("리프레시 토큰 갱신 실패: 이미 폐기된 리프레시 토큰입니다. tokenValue={}", refreshToken);
            throw new CustomException(ErrorCode.REFRESH_TOKEN_MISMATCH); // 또는 TOKEN_ALREADY_REVOKED 등 더 명확한 에러 코드 고려
        }

        // 6. 기존 리프레시 토큰 폐기
        storedRefreshToken.revoke();
        refreshTokenRepository.save(storedRefreshToken); // 변경 감지되므로 save 필요 없을 수도 있지만 명시적으로 호출

        // 7. 새로운 액세스 토큰 및 리프레시 토큰 생성
        // Authentication 객체를 재구성하여 JwtTokenProvider에 전달
        // MemberEntity가 UserDetails를 구현한다고 가정
        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(member.getRole().toString()));
        Authentication authentication = new UsernamePasswordAuthenticationToken(member.getUserId(), null, authorities);

        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
        String newRefreshTokenValue = jwtTokenProvider.generateRefreshToken(authentication);
        LocalDateTime newRefreshTokenExpiryDate = LocalDateTime.now().plus(Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpirationMillis()));

        // 8. 새로운 리프레시 토큰 저장
        RefreshTokenEntity newRefreshTokenEntity = RefreshTokenEntity.builder()
                .memberId(member.getMemberId())
                .tokenValue(newRefreshTokenValue)
                .expiryDate(newRefreshTokenExpiryDate)
                .revoked(false)
                .build();
        refreshTokenRepository.save(newRefreshTokenEntity);

        log.info("리프레시 토큰 갱신 성공: 사용자 ID = {}", userId);

        // 9. TokenResponseDto 반환
        return TokenResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenValue)
                .memberId(member.getMemberId())
                .userId(member.getUserId())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .preferredTradeArea(member.getPreferredTradeArea())
                // .sellingInProgressCount(member.getSellingInProgressCount()) // MemberEntity에 해당 메서드 없음
                // .buyingInProgressCount(member.getBuyingInProgressCount()) // MemberEntity에 해당 메서드 없음
                // .unreadMessageCount(member.getUnreadMessageCount()) // MemberEntity에 해당 메서드 없음
                .build();
    }

    @Override
    public LoginResponseDto getAuthenticatedUserInfo(String accessToken) {
        // TODO: 액세스 토큰에서 사용자 정보 추출 및 DB에서 최신 정보 조회 로직 구현
        log.info("인증된 사용자 정보 조회 요청: {}", accessToken);
        throw new CustomException(ErrorCode.NOT_IMPLEMENTED); // 구현 필요
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
    	log.info("로그아웃 요청: {}", refreshToken);

        // 1. 리프레시 토큰 유효성 검사 (선택 사항이지만, 유효하지 않은 토큰에 대한 불필요한 DB 조회 방지)
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("로그아웃 실패: 유효하지 않은 리프레시 토큰입니다. tokenValue={}", refreshToken);
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 2. DB에서 해당 리프레시 토큰 조회
        RefreshTokenEntity storedRefreshToken = refreshTokenRepository.findByTokenValue(refreshToken)
                .orElseThrow(() -> {
                    log.warn("로그아웃 실패: DB에서 리프레시 토큰을 찾을 수 없습니다. tokenValue={}", refreshToken);
                    throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
                });

        // 3. 토큰 폐기 상태로 변경 및 저장
        if (!storedRefreshToken.isRevoked()) { // 이미 폐기되지 않은 경우에만 처리
            storedRefreshToken.revoke();
            refreshTokenRepository.save(storedRefreshToken);
            log.info("리프레시 토큰이 성공적으로 폐기되었습니다: tokenValue={}", refreshToken);
        } else {
            log.warn("로그아웃 요청된 리프레시 토큰은 이미 폐기된 상태입니다: tokenValue={}", refreshToken);
        }
        // 해당 사용자의 다른 모든 리프레시 토큰을 폐기할 수도 있습니다.
         Long memberId = storedRefreshToken.getMemberId();
         refreshTokenRepository.deleteByMemberId(memberId);
    }

    @Override
    public void requestPasswordReset(String email) {
        // TODO: 비밀번호 재설정 요청 로직 구현 (이메일 발송 등)
        log.info("비밀번호 재설정 요청: 이메일 = {}", email);
        throw new CustomException(ErrorCode.NOT_IMPLEMENTED); // 구현 필요
    }

    @Override
    public void resetPassword(ResetPasswordRequestDto resetPasswordRequestDto) {
        // TODO: 비밀번호 재설정 토큰 확인 및 비밀번호 업데이트 로직 구현
        log.info("비밀번호 재설정 처리");
        throw new CustomException(ErrorCode.NOT_IMPLEMENTED); // 구현 필요
    }

    @Override
    public void verifyEmail(String verificationToken) {
        // TODO: 이메일 인증 토큰 확인 및 사용자 인증 상태 변경 로직 구현
        log.info("이메일 인증 요청: 토큰 = {}", verificationToken);
        throw new CustomException(ErrorCode.NOT_IMPLEMENTED); // 구현 필요
    }

    /**
     * @brief 인증된 사용자의 비밀번호를 변경합니다.
     * 이 메서드는 MemberService의 changeUserPwd를 호출하여 실제 비밀번호 변경을 처리합니다.
     * @param userId 비밀번호를 변경할 사용자의 ID
     * @param changePasswordRequestDto 현재 비밀번호와 새 비밀번호 정보를 포함하는 DTO
     * @throws MemberNotFoundException 회원을 찾을 수 없을 때
     * @throws InvalidPasswordException 현재 비밀번호가 일치하지 않을 때
     * @throws PasswordMismatchException 새 비밀번호와 확인 비밀번호가 일치하지 않을 때
     */
    @Override
    @Transactional
    public void changePassword(String userId, ChangePasswordRequestDto changePasswordRequestDto) {
        // userId를 통해 memberId를 조회 (MemberRepository 또는 MemberService에 해당 메서드 필요)
        MemberEntity member = memberRepository.findByUserId(userId)
                .orElseThrow(MemberNotFoundException::new); // userId로 MemberEntity를 찾고 없으면 예외 발생

        // MemberService의 비밀번호 변경 로직 호출
        memberService.changeUserPwd(member.getMemberId(), changePasswordRequestDto);
        log.info("비밀번호 변경 성공: 사용자 ID = {}", userId);
    }
}
