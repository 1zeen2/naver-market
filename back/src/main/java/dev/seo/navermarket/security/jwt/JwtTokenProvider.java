package dev.seo.navermarket.security.jwt;

import dev.seo.navermarket.member.domain.MemberEntity; // MemberEntity 임포트
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JWT 토큰을 생성하고 검증하는 유틸리티 클래스입니다.
 * 액세스 토큰과 리프레시 토큰을 모두 관리하며, Spring Security와 통합됩니다.
 * JJWT 라이브러리 0.12.x 버전 이상에 맞게 최신 플루언트 API를 사용합니다.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey; // JWT 서명에 사용될 비밀 키
    private final long accessTokenExpirationMillis; // 액세스 토큰 만료 시간 (밀리초)
    private final long refreshTokenExpirationMillis; // 리프레시 토큰 만료 시간 (밀리초)

    /**
     * JwtTokenProvider 생성자입니다.
     * application.properties에서 JWT 관련 설정 값을 주입받습니다.
     *
     * @param secretKeyBase64 JWT 서명에 사용될 비밀 키 (Base64 인코딩된 문자열)
     * @param accessTokenExpirationMillis 액세스 토큰 만료 시간 (밀리초)
     * @param refreshTokenExpirationMillis 리프레시 토큰 만료 시간 (밀리초)
     */
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKeyBase64,
            @Value("${jwt.access-token-expiration-ms}") long accessTokenExpirationMillis,
            @Value("${jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMillis) {
    
        // Base64 디코딩된 비밀 키를 사용하여 SecretKey 객체 생성
        // Keys.hmacShaKeyFor는 주어진 바이트 배열을 사용하여 HMAC SHA 키를 생성합니다.
        this.secretKey = Keys.hmacShaKeyFor(secretKeyBase64.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMillis = accessTokenExpirationMillis;
        this.refreshTokenExpirationMillis = refreshTokenExpirationMillis;
    }

    /**
     * 주어진 인증 정보(Authentication)로 액세스 토큰을 생성합니다.
     * 토큰에는 사용자 ID(subject), 권한(auth), 닉네임(nickname), 역할(role) 클레임이 포함됩니다.
     *
     * @param authentication 토큰에 포함될 인증 정보
     * @return 생성된 액세스 토큰 문자열
     */
    public String generateAccessToken(Authentication authentication) {
        // Authentication 객체에서 권한 정보를 문자열로 변환 (예: "ROLE_USER,ROLE_ADMIN")
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // Authentication 객체의 principal이 MemberEntity 타입이라고 가정합니다.
        // 실제 MemberEntity 객체에서 nickname과 role을 가져와 클레임에 추가합니다.
        // TODO: CustomUserDetailsService에서 MemberEntity 객체를 principal로 반환하도록 구현해야 합니다.
        String nickname = "";
        String role = "";
        if (authentication.getPrincipal() instanceof MemberEntity) {
            MemberEntity member = (MemberEntity) authentication.getPrincipal();
            nickname = member.getNickname();
            role = member.getRole().name(); // Enum이라면 .name()으로 문자열 변환
        } else if (authentication.getPrincipal() instanceof User) {
            // Spring Security User 객체인 경우, subject는 username이 됩니다.
            // nickname과 role은 별도의 클레임으로 추가하기 위해 CustomUserDetailsService에서
            // User 객체를 확장하거나, 별도의 Map으로 클레임을 전달하는 방식 고려 필요
            log.warn("Authentication principal is Spring Security User. Cannot extract nickname/role directly. Consider custom UserDetails.");
            // 임시로 subject를 nickname으로 사용하거나, 다른 방식으로 처리해야 합니다.
            // 여기서는 일단 비워둡니다.
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMillis);

        return Jwts.builder()
                .subject(authentication.getName()) // 토큰의 주체 (사용자 ID 또는 이메일)
                .claim("auth", authorities) // 권한 정보 클레임 추가
                .claim("nickname", nickname) // 닉네임 클레임 추가
                .claim("role", role) // 역할 클레임 추가
                .issuedAt(now) // 토큰 발행 시간
                .expiration(expiryDate) // 토큰 만료 시간
                .signWith(secretKey, Jwts.SIG.HS512) // 서명에 사용할 키와 알고리즘
                .compact(); // JWT 생성
    }

    /**
     * 주어진 인증 정보(Authentication)로 리프레시 토큰을 생성합니다.
     * 액세스 토큰보다 긴 만료 시간을 가집니다.
     * 리프레시 토큰은 주로 사용자 ID(subject)만 포함하며, 권한 등은 포함하지 않는 것이 일반적입니다.
     *
     * @param authentication 토큰에 포함될 인증 정보
     * @return 생성된 리프레시 토큰 문자열
     */
    public String generateRefreshToken(Authentication authentication) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationMillis);

        return Jwts.builder()
                .subject(authentication.getName()) // 토큰의 주체 (사용자 ID 또는 이메일)
                .issuedAt(now) // 토큰 발행 시간
                .expiration(expiryDate) // 토큰 만료 시간
                .signWith(secretKey, Jwts.SIG.HS512) // 서명에 사용할 키와 알고리즘
                .compact(); // JWT 생성
    }

    /**
     * JWT 토큰에서 인증 정보를 추출하여 Spring Security의 Authentication 객체를 반환합니다.
     *
     * @param accessToken JWT 토큰 문자열
     * @return Spring Security Authentication 객체
     * @throws RuntimeException 토큰에 권한 정보가 없거나 유효하지 않을 경우
     */
    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken); // 토큰에서 클레임 추출

        // "auth" 클레임이 없으면 권한 정보가 없는 토큰으로 간주
        if (claims.get("auth") == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 클레임에서 권한 정보를 파싱하여 GrantedAuthority 컬렉션으로 변환
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // UserDetails 객체를 생성하여 Authentication 객체 반환
        // 여기서 User는 org.springframework.security.core.userdetails.User 입니다.
        // subject는 사용자 ID (memberId)
        User principal = new User(claims.getSubject(), "", authorities); // 비밀번호는 필요 없으므로 빈 문자열
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    /**
     * 주어진 JWT 토큰의 유효성을 검증합니다.
     *
     * @param token 유효성을 검증할 JWT 토큰
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true; // 토큰이 성공적으로 파싱되고 서명이 유효하면 true 반환
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다.", e); // info -> warn
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다.", e); // info -> warn
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰입니다.", e); // info -> warn
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 잘못되었습니다.", e); // info -> warn
        }
        return false; // 위에 해당되는 예외 발생 시 false 반환
    }

    /**
     * JWT 토큰에서 클레임(Claim)을 추출합니다.
     * 만료된 토큰의 경우에도 클레임을 추출할 수 있도록 ExpiredJwtException을 처리합니다.
     *
     * @param accessToken 클레임을 추출할 JWT 문자열
     * @return Claims 객체 (토큰에 포함된 모든 정보)
     */
    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            // 토큰이 만료되었더라도 클레임에 접근할 수 있도록 만료된 클레임을 반환합니다.
            return e.getClaims();
        }
    }

    /**
     * 리프레시 토큰의 만료 시간을 반환합니다.
     * @return 리프레시 토큰 만료 시간 (밀리초)
     */
    public long getRefreshTokenExpirationMillis() {
        return refreshTokenExpirationMillis;
    }

    /**
     * JWT 토큰에서 특정 클레임을 추출합니다.
     * 이 메서드는 토큰이 만료되었더라도 클레임에 접근할 수 있도록 parseClaims를 사용합니다.
     * @param token 클레임을 추출할 JWT 문자열
     * @param claimsResolver 추출할 클레임을 정의하는 Function
     * @return 추출된 클레임 값
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * JWT 토큰에서 memberId 클레임을 추출합니다.
     * generateAccessToken/RefreshToken에서 subject로 memberId를 설정했으므로, subject에서 추출합니다.
     * @param token memberId를 추출할 JWT 문자열
     * @return Long memberId
     */
    public Long extractMemberId(String token) {
        // subject는 String 타입이므로 Long으로 파싱합니다.
        return Long.parseLong(extractClaim(token, Claims::getSubject));
    }

    /**
     * JWT 토큰에서 사용자 ID (subject)를 추출합니다.
     * @param token 사용자 ID를 추출할 JWT 문자열
     * @return String 사용자 ID
     */
    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * JWT 토큰에서 nickname 클레임을 추출합니다.
     * @param token nickname을 추출할 JWT 문자열
     * @return String nickname
     */
    public String extractNickname(String token) {
        return extractClaim(token, claims -> claims.get("nickname", String.class));
    }

    /**
     * JWT 토큰에서 role 클레임을 추출합니다.
     * @param token role을 추출할 JWT 문자열
     * @return String role
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }
    
}
