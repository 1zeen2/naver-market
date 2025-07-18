package dev.seo.navermarket.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter; // 요청당 한 번만 실행되는 필터 임포트

import dev.seo.navermarket.security.CustomUserDetailsService;

import java.io.IOException;

/**
 * @file JwtAuthenticationFilter.java
 * @brief JWT 토큰을 검증하고 Spring Security 컨텍스트에 인증 정보를 설정하는 필터입니다.
 * 모든 HTTP 요청에 대해 한 번씩 실행됩니다.
 */
@Component
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성합니다.
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰 생성 및 검증 유틸리티 주입
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * @brief 모든 HTTP 요청에 대해 JWT 토큰을 검증하고 인증을 처리합니다.
     *
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param filterChain 필터 체인 (다음 필터로 요청을 전달)
     * @throws ServletException 서블릿 예외 발생 시
     * @throws IOException 입출력 예외 발생 시
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 1. 요청 헤더에서 JWT 토큰 추출
            String jwt = getJwtFromRequest(request);

            // 2. 토큰 유효성 검증 및 사용자 인증
            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
                // 토큰에서 사용자 ID 추출
                String userId = jwtTokenProvider.extractUserId(jwt);
                log.debug("JWT 토큰에서 추출된 userId: {}", userId);

                 UserDetails userDetails = customUserDetailsService.loadUserByUsername(userId);

                 // UserDetails 객체와 권한 정보를 사용하여 인증 객체 생성
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, userDetails.getAuthorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Spring Security 컨텍스트에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("JWT 토큰으로 사용자 인증 성공: {}", userId);
            } else {
                log.debug("JWT 토큰이 없거나 유효하지 않습니다. 경로: {}", request.getRequestURI());
            }
        } catch (Exception ex) {
            // JWT 관련 예외 발생 시 (예: 토큰 파싱 실패, 서명 오류 등)
            log.error("JWT 인증 필터에서 사용자 인증을 설정할 수 없습니다: {}", ex.getMessage(), ex);
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    /**
     * @brief HTTP 요청에서 JWT 토큰을 추출합니다.
     * Authorization 헤더에서 "Bearer " 접두사를 제거한 후 토큰을 반환합니다.
     *
     * @param request HTTP 요청 객체
     * @return 추출된 JWT 토큰 문자열 또는 null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
