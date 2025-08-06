package dev.seo.navermarket.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 인증된 사용자가 필요한 권한 없이 보호된 리소스에 접근하려 할 때 호출되는 핸들러입니다.
 * HTTP 403 Forbidden 응답을 반환합니다.
 */
@Slf4j
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * 권한이 없는 사용자가 보호된 리소스에 접근하려 할 때 호출됩니다.
     *
     * @param request HttpServletRequest 객체
     * @param response HttpServletResponse 객체
     * @param accessDeniedException 발생한 접근 거부 예외
     * @throws IOException 응답 작성 중 발생할 수 있는 예외
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        log.warn("Access denied for user: {}", accessDeniedException.getMessage());

        // 403 Forbidden 상태 코드 설정
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        // 응답 Content-Type을 JSON으로 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // 응답 문자 인코딩 설정
        response.setCharacterEncoding("UTF-8");

        // 클라이언트에게 보낼 JSON 응답 본문 생성
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_FORBIDDEN);
        body.put("error", "Forbidden");
        body.put("message", accessDeniedException.getMessage()); // 예외 메시지 포함
        body.put("path", request.getServletPath()); // 요청 경로 포함

        // JSON 응답 작성
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}
