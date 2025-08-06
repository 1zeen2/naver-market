package dev.seo.navermarket.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 인증되지 않은 사용자가 보호된 리소스에 접근하려 할 때 호출되는 핸들러입니다.
 * HTTP 401 Unauthorized 응답을 반환합니다.
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * 인증되지 않은 사용자가 보호된 리소스에 접근하려 할 때 호출됩니다.
     *
     * @param request HttpServletRequest 객체
     * @param response HttpServletResponse 객체
     * @param authException 발생한 인증 예외
     * @throws IOException 응답 작성 중 발생할 수 있는 예외
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        log.warn("Unauthorized access attempt: {}", authException.getMessage());

        // 401 Unauthorized 상태 코드 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // 응답 Content-Type을 JSON으로 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // 응답 문자 인코딩 설정
        response.setCharacterEncoding("UTF-8");

        // 클라이언트에게 보낼 JSON 응답 본문 생성
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", authException.getMessage()); // 예외 메시지 포함
        body.put("path", request.getServletPath()); // 요청 경로 포함

        // JSON 응답 작성
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}
