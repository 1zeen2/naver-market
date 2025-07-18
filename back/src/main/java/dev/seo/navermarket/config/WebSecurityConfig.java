package dev.seo.navermarket.config;

import dev.seo.navermarket.jwt.JwtAuthenticationFilter; // JWT 인증 필터 임포트
import dev.seo.navermarket.jwt.JwtTokenProvider; // JWT 토큰 프로바이더 임포트
import dev.seo.navermarket.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // CSRF 비활성화를 위한 임포트
import org.springframework.security.config.http.SessionCreationPolicy; // 세션 관리 정책 임포트
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // BCryptPasswordEncoder 임포트
import org.springframework.security.crypto.password.PasswordEncoder; // PasswordEncoder 인터페이스 임포트
import org.springframework.security.web.SecurityFilterChain; // SecurityFilterChain 임포트
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // 필터 추가 위치 지정 임포트
import org.springframework.web.cors.CorsConfiguration; // CORS 설정 임포트
import org.springframework.web.cors.CorsConfigurationSource; // CORS 설정 소스 임포트
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // URL 기반 CORS 설정 소스 임포트

import java.util.Arrays; // Arrays 유틸리티 임포트
import java.util.List; // List 유틸리티 임포트

/**
 * @file WebSecurityConfig.java
 * @brief Spring Security 설정을 담당하는 클래스입니다.
 * JWT(JSON Web Token) 기반의 인증 및 권한 부여를 구성합니다.
 */
@Configuration
@EnableWebSecurity	// Spring Security를 활성화하고 웹 보안 구성을 가능하게 합니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성하여 의존성 주입을 처리합니다.
public class WebSecurityConfig {
	
	private final JwtTokenProvider jwtTokenProvider; // JWT 토큰 생성 및 검증을 위한 프로바이더 주입
	private final CustomUserDetailsService customUserDetailsService;
	
	/**
     * @brief 비밀번호 암호화를 위한 PasswordEncoder 빈을 등록합니다.
     * BCryptPasswordEncoder는 강력한 해싱 알고리즘을 사용하여 비밀번호를 안전하게 저장합니다.
     *
     * @return BCryptPasswordEncoder 인스턴스
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
	
	/**
     * @brief AuthenticationManager 빈을 등록합니다.
     * 이 매니저는 Spring Security의 인증 프로세스를 처리합니다.
     *
     * @param authenticationConfiguration Spring Security의 인증 구성 객체
     * @return AuthenticationManager 인스턴스
     * @throws Exception 인증 관리자 가져오기 실패 시 예외 발생
     */
	@Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
	
	/**
     * @brief CORS(Cross-Origin Resource Sharing) 설정을 정의합니다.
     * 프론트엔드와 백엔드가 다른 도메인/포트에서 통신할 때 발생하는 CORS 문제를 해결합니다.
     *
     * @return CorsConfigurationSource 인스턴스
     */
	@Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 허용할 오리진(Origin) 목록을 설정합니다. 개발 환경에서는 프론트엔드 URL을 명시합니다.
        // 실 서비스 환경에서는 실제 프론트엔드 도메인을 명시해야 합니다.
        configuration.setAllowedOrigins(List.of("http://localhost:3000")); // Next.js 개발 서버 포트
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")); // 허용할 HTTP 메서드
        configuration.setAllowedHeaders(List.of("*")); // 모든 헤더 허용
        configuration.setAllowCredentials(true); // 자격 증명(쿠키, Authorization 헤더 등) 허용
        configuration.setMaxAge(3600L); // Pre-flight 요청 캐시 시간 (1시간)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 CORS 설정 적용
        return source;
    }
	
	/**
     * @brief Spring Security 필터 체인을 구성합니다.
     * HTTP 요청에 대한 보안 규칙, 필터 순서 등을 정의합니다.
     *
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain 인스턴스
     * @throws Exception 보안 구성 실패 시 예외 발생
     */
	@Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			// CORS 설정 적용: 위에서 정의한 corsConfigurationSource 빈을 사용합니다.
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // CSRF(Cross-Site Request Forgery) 보호 비활성화:
            // JWT는 세션 기반이 아니므로 CSRF 공격에 대한 추가적인 보호가 필요하지 않습니다.
            .csrf(AbstractHttpConfigurer::disable)
            // 예외 처리 설정: 인증/인가 실패 시 처리할 핸들러를 정의할 수 있습니다.
            // .exceptionHandling(exceptionHandling -> exceptionHandling
            //     .authenticationEntryPoint(jwtAuthenticationEntryPoint) // 인증 실패 시
            //     .accessDeniedHandler(jwtAccessDeniedHandler) // 인가 실패 시
            // )
            // 세션 관리 설정: JWT는 무상태(Stateless)이므로 세션을 사용하지 않도록 설정합니다.
            .sessionManagement(sessionManagement -> sessionManagement
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // 요청별 인가 규칙 설정:
            .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                // 인증 및 회원가입 관련 API는 모두 허용 (인증 없이 접근 가능)
                .requestMatchers("/api/auth/**").permitAll()
                // 회원 관련 API(아이디, 이메일 중복 확인 등)도 허용
                .requestMatchers("/api/member/**").permitAll()
                // 상품 목록 조회 API는 모두 허용 (인증 없이 접근 가능)
                .requestMatchers("/api/products", "/api/products/**").permitAll()
                // H2 Console (개발용 DB 콘솔) 접근 허용 (개발 시에만 사용)
                // .requestMatchers("/h2-console/**").permitAll()
                // 그 외 모든 요청은 인증된 사용자만 허용
                .anyRequest().authenticated()
            )
            // JWT 인증 필터 추가:
            // Spring Security의 UsernamePasswordAuthenticationFilter 이전에 JWT 인증 필터를 추가합니다.
            // 이렇게 함으로써 JWT 토큰을 먼저 검증하고, 유효하면 SecurityContext에 인증 정보를 설정합니다.
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
