package com.asdf.minilog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI 문서화 설정 클래스.
 *
 * <p>API 메타데이터와 JWT Bearer 인증 스키마를 등록하여 Swagger UI에서 인증된 요청을 테스트할 수 있도록 한다.
 */
@Configuration
public class ApiDocumentationConfig {

  /**
   * OpenAPI 스펙 빈을 생성한다.
   *
   * <p>API 제목/설명/버전 정보와 함께 "bearerAuth"(JWT) 보안 스키마를 등록한다.
   *
   * @return 구성된 {@link OpenAPI} 객체
   */
  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info().title("Minilog API").description("API for Minilog").version("v2"))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
        .components(
            new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
  }
}
