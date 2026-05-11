package com.asdf.minilog.config;

// JWT 인증 실패 시 처리할 엔트리포인트 클래스
import com.asdf.minilog.security.JwtAuthenticationEntryPoint;
// 모든 HTTP 요청에서 JWT 토큰을 검증하는 필터 클래스
import com.asdf.minilog.security.JwtRequestFilter;
// 생성자 주입을 위한 어노테이션
import org.springframework.beans.factory.annotation.Autowired;
// 스프링 빈 등록을 위한 어노테이션
import org.springframework.context.annotation.Bean;
// 스프링 설정 클래스임을 나타내는 어노테이션
import org.springframework.context.annotation.Configuration;
// HTTP 메서드(GET, POST, DELETE 등)를 지정하기 위한 열거형
import org.springframework.http.HttpMethod;
// 인증 처리를 담당하는 핵심 인터페이스
import org.springframework.security.authentication.AuthenticationManager;
// AuthenticationManager를 가져오기 위한 설정 클래스
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
// @PreAuthorize, @PostAuthorize 등 메서드 수준 보안을 활성화하는 어노테이션
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
// HTTP 보안 설정을 구성하기 위한 빌더 클래스
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// Spring Security의 웹 보안 기능을 활성화하는 어노테이션
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// CSRF 등 보안 설정을 비활성화할 때 사용하는 추상 클래스
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
// 세션 생성 정책을 정의하는 열거형 (STATELESS, IF_REQUIRED 등)
import org.springframework.security.config.http.SessionCreationPolicy;
// BCrypt 해싱 알고리즘을 사용하는 비밀번호 인코더
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// 비밀번호 인코딩을 위한 인터페이스
import org.springframework.security.crypto.password.PasswordEncoder;
// Spring Security 필터 체인을 구성하는 인터페이스
import org.springframework.security.web.SecurityFilterChain;
// 폼 기반 로그인 인증을 처리하는 기본 필터 (JWT 필터의 삽입 위치 기준점으로 사용)
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// 이 클래스가 스프링 설정 클래스임을 선언
@Configuration
// Spring Security의 웹 보안 기능을 활성화
@EnableWebSecurity
// 메서드 수준 보안(@PreAuthorize 등)을 활성화, prePostEnabled=true로 사전/사후 인가 검사 허용
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

  // 인증되지 않은 요청에 대한 응답 처리를 담당하는 엔트리포인트
  private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  // 매 요청마다 JWT 토큰을 파싱하고 인증 정보를 SecurityContext에 설정하는 필터
  private JwtRequestFilter jwtRequestFilter;

  // 생성자 주입: 스프링이 자동으로 JwtAuthenticationEntryPoint와 JwtRequestFilter 빈을 주입
  @Autowired
  public SecurityConfig(
      JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint, JwtRequestFilter jwtRequestFilter) {
    // 주입받은 엔트리포인트를 필드에 할당
    this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    // 주입받은 JWT 필터를 필드에 할당
    this.jwtRequestFilter = jwtRequestFilter;
  }

  // 비밀번호 인코더를 스프링 빈으로 등록
  @Bean
  public PasswordEncoder passwordEncoder() {
    // BCrypt 해싱 알고리즘을 사용하여 비밀번호를 암호화 (솔트를 자동 생성하여 레인보우 테이블 공격 방지)
    return new BCryptPasswordEncoder();
  }

  // AuthenticationManager를 스프링 빈으로 등록 (로그인 시 사용자 인증 처리에 사용)
  @Bean
  public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration configuration)
      throws Exception {
    // AuthenticationConfiguration에서 이미 구성된 AuthenticationManager를 가져와 반환
    return configuration.getAuthenticationManager();
  }

  // Spring Security 필터 체인을 스프링 빈으로 등록 (HTTP 보안 설정의 핵심)
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
        // CSRF 보호를 비활성화 (JWT 기반 인증은 STATELESS이므로 CSRF 토큰이 불필요)
        .csrf(AbstractHttpConfigurer::disable)
        // URL 별 접근 권한 규칙을 설정
        .authorizeHttpRequests(
            (requests) ->
                requests
                    // 로그인 API, Swagger UI, API 문서 경로는 인증 없이 접근 허용
                    .requestMatchers("/api/v2/auth/login", "/swagger-ui/**", "/v3/api-docs/**")
                    .permitAll()
                    // 사용자 생성, 조회는 인증 없이 가능하도록 조치.
                    // 회원가입(POST /api/v2/user)은 인증 없이 접근 허용
                    .requestMatchers(HttpMethod.POST, "/api/v2/user")
                    .permitAll()
                    // 사용자 조회(GET /api/v2/user/{userId})는 인증 없이 접근 허용
                    .requestMatchers(HttpMethod.GET, "/api/v2/user/{userId}")
                    .permitAll()
                    // 사용자 삭제는 어드민 권한이 필요하도록 조치.
                    // 사용자 삭제(DELETE /api/v2/user/{userId})는 ROLE_ADMIN 권한을 가진 사용자만 접근 가능
                    .requestMatchers(HttpMethod.DELETE, "/api/v2/user/{userId}")
                    .hasRole("ADMIN")
                    // 위에서 명시하지 않은 나머지 모든 요청은 인증이 필요
                    .anyRequest()
                    .authenticated())
        // 인증 예외 처리 설정: 인증되지 않은 요청 시 jwtAuthenticationEntryPoint가 응답 처리
        .exceptionHandling(
            exceptionHandling ->
                exceptionHandling.authenticationEntryPoint(jwtAuthenticationEntryPoint))
        // 세션 관리 설정: STATELESS로 설정하여 서버가 세션을 생성하지 않음 (JWT로 상태를 관리)
        .sessionManagement(
            sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 배치하여, 폼 로그인 전에 JWT 인증을 먼저 수행
    httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    // 설정이 완료된 SecurityFilterChain 객체를 빌드하여 반환
    return httpSecurity.build();
  }
}
