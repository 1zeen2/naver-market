package dev.seo.navermarket.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter; // 요청당 한 번만 실행되는 필터 임포트

import dev.seo.navermarket.security.CustomUserDetailsService;
import io.jsonwebtoken.JwtException;

import java.io.IOException;

/**
 * @file JwtAuthenticationFilter.java
 * @brief JWT 토큰을 검증하고 Spring Security 컨텍스트에 인증 정보를 설정하는 필터입니다.
 * 모든 HTTP 요청에 대해 한 번씩 실행됩니다.
 */
@Slf4j
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성합니다.
public class JwtAuthenticationFilter extends OncePerRequestFilter {


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

        // 1. 요청 URI 로그 (필터가 어떤 요청에 대해 동작하는지 확인)
        log.debug("### JwtAuthenticationFilter.doFilterInternal - Request URI: {}", request.getRequestURI());

        String jwt = null;
        String authorizationHeader = request.getHeader("Authorization");

        // 2. Authorization 헤더 존재 여부 및 값 로그
        log.debug("### JwtAuthenticationFilter - Received Authorization header: {}", 
                  authorizationHeader != null ? authorizationHeader.substring(0, Math.min(authorizationHeader.length(), 50)) + "..." : "null");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            // 3. 추출된 JWT 토큰 로그 (보안을 위해 일부만 로깅)
            log.debug("### JwtAuthenticationFilter - Extracted JWT: {}", jwt.substring(0, Math.min(jwt.length(), 20)) + "...");
        } else {
            log.debug("### JwtAuthenticationFilter - Authorization header missing or not starting with Bearer. Skipping JWT authentication.");
        }

        if (jwt != null) {
            try {
                if (jwtTokenProvider.validateToken(jwt)) {
                    String userId = jwtTokenProvider.extractUserId(jwt);
                    log.debug("### JwtAuthenticationFilter - Token is valid. Extracted userId: {}", userId);

                    // 이미 인증된 사용자인지 확인 (중복 인증 방지)
                    if (SecurityContextHolder.getContext().getAuthentication() == null || 
                        !SecurityContextHolder.getContext().getAuthentication().getName().equals(userId)) {
                        
                        UserDetails userDetails = customUserDetailsService.loadUserByUsername(userId);
                        if (userDetails != null) {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            log.info("### JwtAuthenticationFilter - User '{}' authenticated successfully. SecurityContextHolder updated.", userId);
                        } else {
                            log.warn("### JwtAuthenticationFilter - UserDetails could not be loaded for userId: {}", userId);
                        }
                    } else {
                        log.debug("### JwtAuthenticationFilter - User '{}' is already authenticated. Skipping authentication.", userId);
                    }
                } else {
                    log.warn("### JwtAuthenticationFilter - JWT token is invalid or expired. Path: {}", request.getRequestURI());
                }
            } catch (JwtException e) {
                // JWT 토큰 관련 구체적인 예외 (서명 오류, 만료 등)
                log.warn("### JwtAuthenticationFilter - JWT exception for URI {}: {}", request.getRequestURI(), e.getMessage());
            } catch (Exception e) {
                // 그 외 예상치 못한 예외
                log.error("### JwtAuthenticationFilter - An unexpected error occurred during authentication for URI {}: {}", request.getRequestURI(), e.getMessage(), e);
            }
        } else {
             // JWT가 null인 경우에도, 요청이 필터 체인을 계속 통과하도록 합니다.
             // permitAll() 경로 등에 대해서는 인증이 필요 없으므로 SecurityContextHolder가 비어있어도 됩니다.
            log.debug("### JwtAuthenticationFilter - No JWT token found in request. Path: {}", request.getRequestURI());
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
        log.debug("### JwtAuthenticationFilter.doFilterInternal - Finished processing for URI: {}", request.getRequestURI());
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
        log.debug("Jwt Filter: 헤더에서 받은 인증:{}", bearerToken != null ? bearerToken : null);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
