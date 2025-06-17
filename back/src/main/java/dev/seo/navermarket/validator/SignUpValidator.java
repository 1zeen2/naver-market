package dev.seo.navermarket.validator;

import org.springframework.stereotype.Component;

import dev.seo.navermarket.dao.MemberRepositoryDAO;
import dev.seo.navermarket.entity.MemberEntity;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SignUpValidator {
	
	private final MemberRepositoryDAO memberDAO;
	private final PasswordValidator passwordValidator;
	
	// 회원 가입 시 중복 검사
	public void validateSignUp(MemberEntity member) {
		// 아이디 중복 검사
		if (memberDAO.existsByUserId(member.getUserId())) {
			throw new IllegalArgumentException("이미 존재하는 아이디 입니다.");
		}
		
		// 이메일 중복 검사
		if (memberDAO.existsByEmail(member.getEmail())) {
			throw new IllegalArgumentException("이미 가입된 이메일 입니다.");
		}
		
		// 비밀번호 유효성 검사
		passwordValidator.validateUserPwd(member.getUserPwd());
	}

}
