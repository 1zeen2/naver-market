package dev.seo.navermarket.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * @file JwtProperties.java
 * @brief application.yml의 'jwt' 속성을 바인딩하는 설정 클래스입니다.
 * IDE의 자동 완성 및 유효성 검사 메타데이터 생성을 위해 사용됩니다.
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {
    private String secret;
    private Long expiration;
}
