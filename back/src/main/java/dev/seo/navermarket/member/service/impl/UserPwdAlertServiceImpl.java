package dev.seo.navermarket.member.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import dev.seo.navermarket.member.service.UserPwdAlertService;

@Service
public class UserPwdAlertServiceImpl implements UserPwdAlertService {
	
	// 6개월
	private static final long USERPWD_EXPIRATION_PERIOD = 6L;

	@Override
	public boolean isUserPwdExpired(LocalDateTime lastUserPwdChangeDate) {
		// 비밀번호 변경 이력이 없는 경우 비밀번호가 만료된 것으로 간주하는 조건문
		if (lastUserPwdChangeDate == null) {
			return true;
		}
		
		LocalDateTime expirationDateTime = lastUserPwdChangeDate.plusMonths(USERPWD_EXPIRATION_PERIOD);
		
		return LocalDateTime.now().isAfter(expirationDateTime);
	}

}
