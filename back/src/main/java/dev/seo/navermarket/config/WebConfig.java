package dev.seo.navermarket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // 이 클래스가 Spring의 설정 클래스임을 명시
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // /api/로 시작하는 모든 경로에 대해 CORS 허용
                .allowedOrigins("http://localhost:3000") // 배포 시에는 실제 도메인 주소로 변경해야 함
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(true) // 자격 증명 (쿠키, HTTP 인증 등) 허용
                .maxAge(3600); // Pre-flight 요청의 결과를 1시간 동안 캐시
    }
}