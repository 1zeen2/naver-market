package dev.seo.navermarket.config;

import dev.seo.navermarket.security.CustomUserDetailsService;
import dev.seo.navermarket.security.jwt.JwtAccessDeniedHandler;
import dev.seo.navermarket.security.jwt.JwtAuthenticationEntryPoint;
import dev.seo.navermarket.security.jwt.JwtAuthenticationFilter;
import dev.seo.navermarket.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
// === 추가된 임포트 ===
import org.springframework.security.web.context.SecurityContextHolderFilter;
// ====================
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * @file WebSecurityConfig.java
 * @brief Spring Security 설정을 담당하는 클래스입니다.
 * JWT(JSON Web Token) 기반의 인증 및 권한 부여를 구성합니다.
 */
@Configuration
@EnableWebSecurity	// Spring Security를 활성화하고 웹 보안 구성을 가능하게 합니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성하여 의존성 주입을 처리합니다.
public class WebSecurityConfig implements WebMvcConfigurer {
	
	private final JwtTokenProvider jwtTokenProvider; // JWT 토큰 생성 및 검증을 위한 프로바이더 주입
	private final CustomUserDetailsService customUserDetailsService;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
	
	
	// application.yml에서 파일 업로드 디렉토리 경로를 주입받습니다.
	@Value("${file.upload-dir}") // 파일 업로드 디렉토리 경로 주입
	private String uploadDir;
	
	@Bean
	JwtAuthenticationFilter jwtAuthenticationFilter() {
		return new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService);
	}
	
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
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // CSRF(Cross-Site Request Forgery) 보호 비활성화:
			.csrf(AbstractHttpConfigurer::disable)
            // 예외 처리 핸들러 설정
			.exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(jwtAuthenticationEntryPoint) // 인증 실패 시 (401)
                .accessDeniedHandler(jwtAccessDeniedHandler) // 인가 실패 시 (403)
            )
            // 세션 관리 설정: JWT는 무상태(Stateless)이므로 세션을 사용하지 않도록 설정합니다.
			.sessionManagement(sessionManagement -> sessionManagement
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
			.anonymous(AbstractHttpConfigurer::disable)
            // 요청별 인가 규칙 설정:
			.authorizeHttpRequests(authorizeRequests -> authorizeRequests
            		.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            		// 인증 및 회원가입 관련 API는 모두 허용 (인증 없이 접근 가능)
	                .requestMatchers("/api/auth/signup", "/api/auth/login").permitAll() 
	                // 로그인된 사용자 정보 조회는 인증 필요
	                .requestMatchers("/api/auth/me").authenticated() 
	                
	                // 회원 관련 중복 확인 API는 허용 (인증 없이 접근 가능)
	                .requestMatchers("/api/member/check-id", "/api/member/check-email").permitAll() 
	                // 비밀번호 변경 및 만료 확인은 인증 필요
	                .requestMatchers("/api/member/{memberId}/userPwd", "/api/member/{memberId}/userPwdExpired").authenticated()
	                
	                .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll() // 상품 목록 조회 API는 모두 허용 (인증 없이 접근 가능)
	                .requestMatchers(HttpMethod.POST, "/api/products").authenticated() // 상품 등록 (POST)은 인증 필요
	                .requestMatchers(HttpMethod.PUT, "/api/products/**").authenticated()
	                .requestMatchers(HttpMethod.DELETE, "/api/products/**").authenticated()
	                .requestMatchers("/uploads/**").permitAll() 
	                // .requestMatchers("/h2-console/**").permitAll()
	                .anyRequest().authenticated() // 그 외 모든 요청은 인증된 사용자만 허용
            )
            // JWT 인증 필터 추가:
            // Spring Security의 SecurityContextHolderFilter 이전에 JWT 인증 필터를 추가합니다.
            // 이렇게 함으로써 JWT 토큰을 먼저 검증하고, 유효하면 SecurityContext에 인증 정보를 설정합니다.
            // === 변경된 부분 ===
			.addFilterBefore(jwtAuthenticationFilter(), SecurityContextHolderFilter.class);

        return http.build();
    }
	
	/**
	 * @brief 정적 리소스 핸들러를 추가합니다.
	 * "/uploads/**" URL 패턴으로 들어오는 요청을 실제 파일 시스템의 업로드 디렉토리로 매핑합니다.
	 * 이렇게 함으로써 프론트엔드에서 `/uploads/파일명.jpg`와 같은 URL로 이미지에 접근할 수 있게 됩니다.
	 *
	 * @param registry ResourceHandlerRegistry 객체
	 */
	@Override // WebMvcConfigurer 인터페이스의 메서드 오버라이드
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/uploads/**")
				.addResourceLocations("file:///" + uploadDir + "/");
	}
	
}