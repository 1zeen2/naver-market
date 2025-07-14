package dev.seo.navermarket.jwt;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;



/**
 * @file JwtTokenProvider.java
 * @brief JWT 토큰 생성, 유효성 검사, 정보 추출을 담당하는 유틸리티 클래스입니다.
 * application.properties에서 JWT 비밀 키와 만료 시간을 설정합니다.
 */
@Component
public class JwtTokenProvider {
	
	private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
	
	// application.yml에서 주입받을 JWT 비밀 키 (Base64 인코딩된 문자열)
	@Value("${jwt.secret}")
    private String secret;
	
	// application.properties에서 주입받을 JWT 만료 시간
	@Value("${jwt.expiration}")
    private long expiration;
	
	// JWT 서명에 사용될 SecretKey 객체 (Base64 디코딩 후 생성)
	private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
	
	// --- 핵심 기능 메서드 (Core Public APIs) ---
	
	/**
     * @brief 사용자 ID를 기반으로 JWT 토큰을 생성합니다.
     * @param userId 토큰에 포함될 사용자 ID (subject)
     * @param memberId 토큰에 포함될 회원 ID (claim)
     * @param userName 토큰에 포함될 사용자 이름 (claim)
     * @return 생성된 JWT 문자열
     */
	public String generateToken(Long memberId, String userId, String userName) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("memberId", memberId);
		claims.put("userName", userName);
		
		return Jwts.builder()
                .claims(claims) // 사용자 정의 클레임 설정
                .subject(userId) // 토큰의 주체 (여기서는 사용자 ID)
                .issuedAt(new Date(System.currentTimeMillis())) // 발행 시간
                .expiration(new Date(System.currentTimeMillis() + expiration)) // 만료 시간
                .signWith(getSigningKey(), Jwts.SIG.HS256) // 서명 알고리즘 및 키
                .compact(); // 토큰 생성
				
	}
	
	/**
     * @brief JWT 토큰의 유효성을 검사합니다.
     * @param token 검사할 JWT 문자열
     * @return boolean 토큰이 유효하면 true, 그렇지 않으면 false
     */
	public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey()) // 서명 키로 검증
                .build()
                .parseSignedClaims(token); // 토큰 파싱 및 유효성 검사
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.", e);
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.", e);
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.", e);
        }
        return false;
    }
	
	// --- 클레임 추출 관련 메서드 (Claim Extraction APIs) ---
	
	/**
     * @brief JWT 토큰에서 모든 클레임을 추출합니다.
     * @param token 클레임을 추출할 JWT 문자열
     * @return Claims 객체 (토큰에 포함된 모든 정보)
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload(); // JWS(JSON Web Signature)의 페이로드 (클레임) 추출
    }

    /**
     * @brief JWT 토큰에서 특정 클레임을 추출합니다.
     * @param token 클레임을 추출할 JWT 문자열
     * @param claimsResolver 추출할 클레임을 정의하는 Function
     * @return 추출된 클레임 값
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /// --- 내부 헬퍼 메서드 (Private Helpers) ---
    
    /**
     * @brief JWT 토큰에서 memberId 클레임을 추출합니다.
     * @param token memberId를 추출할 JWT 문자열
     * @return Long memberId
     */
    public Long extractMemberId(String token) {
    	return extractClaim(token, claims -> claims.get("memberId", Long.class));
    }

    /**
     * @brief JWT 토큰에서 사용자 ID (subject)를 추출합니다.
     * @param token 사용자 ID를 추출할 JWT 문자열
     * @return String 사용자 ID
     */
    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * @brief JWT 토큰에서 userName 클레임을 추출합니다.
     * @param token userName을 추출할 JWT 문자열
     * @return String userName
     */
    public String extractUserName(String token) {
        return extractClaim(token, claims -> claims.get("userName", String.class));
    }
}