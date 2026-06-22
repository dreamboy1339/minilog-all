package com.asdf.minilog.security;

import com.asdf.minilog.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 매 요청마다 JWT를 검증하는 필터.
 *
 * <p>{@code Authorization: Bearer <token>} 헤더에서 JWT를 추출해 사용자명을 파싱하고, 토큰이 유효하면 인증 정보를 {@link
 * SecurityContextHolder}에 설정한다. {@link OncePerRequestFilter}를 상속하여 요청당 한 번만 실행된다.
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

  @Autowired private UserDetailsService jwtUserDetailsService;

  @Autowired private JwtUtil jwtUtil;

  /**
   * 요청 헤더의 JWT를 검증하고, 유효하면 SecurityContext에 인증 정보를 등록한 뒤 다음 필터로 진행한다.
   *
   * @param request 현재 요청
   * @param response 응답
   * @param filterChain 다음 필터 체인
   */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String requestTokenHeader = request.getHeader("Authorization");
    String username = null;
    String jwt = null;
    if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
      jwt = requestTokenHeader.substring(7);
      try {
        username = jwtUtil.getUsernameFromToken(jwt);
      } catch (IllegalArgumentException e) {
        logger.error("Unable to get JWT Token", e);
      } catch (ExpiredJwtException e) {
        logger.error("JWT has expired", e);
      }
    } else {
      logger.warn("JWT Token does not begin with Bearer String");
    }

    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      logger.info("Valid JWT Token found for user: {}", username);
      UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(username);
      if (jwtUtil.validateToken(jwt, userDetails)) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        usernamePasswordAuthenticationToken.setDetails(
            new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
      } else {
        logger.error("JWT Token is invalid");
      }
    }
    filterChain.doFilter(request, response);
  }
}
