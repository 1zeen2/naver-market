package dev.seo.navermarket.service;

import java.time.LocalDateTime;

public interface UserPwdAlertService {
    boolean isUserPwdExpired(LocalDateTime lastUserPwdChangeDate);
}
