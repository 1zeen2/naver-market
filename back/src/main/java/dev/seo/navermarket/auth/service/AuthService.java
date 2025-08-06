package dev.seo.navermarket.auth.service;

import dev.seo.navermarket.member.dto.LoginResponseDto;
import dev.seo.navermarket.member.dto.SignupRequestDto;
import dev.seo.navermarket.member.dto.SignupResponseDto;
import dev.seo.navermarket.member.dto.ResetPasswordRequestDto;
import dev.seo.navermarket.auth.dto.LoginRequestDto;
import dev.seo.navermarket.member.dto.ChangePasswordRequestDto; // 추가된 DTO
import dev.seo.navermarket.member.exception.DuplicateEmailException; // 추가
import dev.seo.navermarket.member.exception.DuplicateNicknameException; // 추가
import dev.seo.navermarket.member.exception.DuplicateUserIdException; // 추가
import dev.seo.navermarket.member.exception.InvalidPasswordException; // 추가
import dev.seo.navermarket.member.exception.MemberNotFoundException; // 추가
import dev.seo.navermarket.member.exception.PasswordMismatchException; // 추가
import dev.seo.navermarket.security.dto.TokenResponseDto;

/**
 * @file AuthService.java
 * @brief 인증 관련 비즈니스 로직을 정의하는 서비스 인터페이스입니다.
 * 회원가입, 로그인, 토큰 갱신, 사용자 정보 조회 등의 기능을 제공합니다.
 * 또한, 로그아웃, 비밀번호 재설정, 이메일 인증, 비밀번호 변경과 같은 추가 인증 관련 기능을 포함합니다.
 */
public interface AuthService {
    /**
     * 사용자 회원가입을 처리합니다.
     * @param signupRequestDto 회원가입 요청 정보
     * @return SignupResponseDto 회원가입 성공 시 반환되는 정보
     * @throws DuplicateUserIdException 아이디 중복 시
     * @throws DuplicateNicknameException 닉네임 중복 시
     * @throws DuplicateEmailException 이메일 중복 시
     */
    SignupResponseDto signup(SignupRequestDto signupRequestDto) throws DuplicateUserIdException, DuplicateNicknameException, DuplicateEmailException;

    /**
     * 사용자 로그인을 처리하고, 액세스 토큰과 리프레시 토큰을 발급합니다.
     * @param loginRequestDto 로그인 요청 정보 (사용자 ID, 비밀번호)
     * @return TokenResponseDto 로그인 성공 시 반환되는 토큰 및 사용자 정보
     */
    TokenResponseDto login(LoginRequestDto loginRequestDto);

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급합니다.
     * @param refreshToken 클라이언트가 보낸 리프레시 토큰
     * @return 새로 발급된 토큰 및 사용자 기본 정보를 포함하는 TokenResponseDto
     */
    TokenResponseDto refreshTokens(String refreshToken);

    /**
     * JWT 토큰에서 사용자 정보를 추출하여 LoginResponseDto 형태로 반환합니다.
     * @param accessToken 유효한 JWT 토큰 문자열
     * @return LoginResponseDto 토큰에서 추출된 사용자 정보 및 DB에서 조회된 최신 정보
     */
    LoginResponseDto getAuthenticatedUserInfo(String accessToken);

    /**
     * @brief 사용자를 로그아웃 처리하고, 서버에 저장된 리프레시 토큰을 무효화합니다.
     * @param refreshToken 클라이언트가 보낸 리프레시 토큰
     */
    void logout(String refreshToken);

    /**
     * @brief 비밀번호 재설정을 위해 사용자에게 이메일을 발송하도록 요청합니다.
     * @param email 비밀번호 재설정을 요청하는 사용자의 이메일 주소
     */
    void requestPasswordReset(String email);

    /**
     * @brief 비밀번호 재설정 토큰을 확인하고 새로운 비밀번호로 업데이트합니다.
     * @param resetPasswordRequestDto 재설정 토큰과 새로운 비밀번호 정보를 포함하는 DTO
     */
    void resetPassword(ResetPasswordRequestDto resetPasswordRequestDto);

    /**
     * @brief 이메일 인증 토큰을 사용하여 사용자 이메일 인증을 완료합니다.
     * @param verificationToken 이메일 인증을 위한 토큰
     */
    void verifyEmail(String verificationToken);

    /**
     * @brief 인증된 사용자의 비밀번호를 변경합니다.
     * @param userId 비밀번호를 변경할 사용자의 ID
     * @param changePasswordRequestDto 현재 비밀번호와 새 비밀번호 정보를 포함하는 DTO
     * @throws MemberNotFoundException 회원을 찾을 수 없을 때
     * @throws InvalidPasswordException 현재 비밀번호가 일치하지 않을 때
     * @throws PasswordMismatchException 새 비밀번호와 확인 비밀번호가 일치하지 않을 때
     */
    void changePassword(String userId, ChangePasswordRequestDto changePasswordRequestDto) throws MemberNotFoundException, InvalidPasswordException, PasswordMismatchException;
}
