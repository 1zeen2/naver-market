package dev.seo.navermarket.member.service;

import java.time.LocalDateTime;

public interface UserPwdAlertService {
    boolean isUserPwdExpired(LocalDateTime lastUserPwdChangeDate);
}
