package com.asdf.minilog.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * 인증되지 않은 요청에 대한 진입점(EntryPoint).
 *
 * <p>유효한 JWT가 없거나 인증에 실패한 요청이 보호된 리소스에 접근하면, 401(Unauthorized) 상태와 JSON 에러 메시지를 응답한다.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  /**
   * 인증 실패 시 401 상태 코드와 JSON 형식의 에러 메시지를 응답한다.
   *
   * @param request 현재 요청
   * @param response 응답
   * @param authException 발생한 인증 예외
   */
  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException, ServletException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    Map<String, String> responseBody = new HashMap<>();
    responseBody.put("message", "Unauthorized");

    ObjectMapper objectMapper = new ObjectMapper();
    String jsonResponse = objectMapper.writeValueAsString(responseBody);
    response.getWriter().write(jsonResponse);
  }
}
