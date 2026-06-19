package com.asdf.minilog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 활성화 설정 클래스.
 *
 * <p>{@code @EnableJpaAuditing}을 통해 엔티티의 생성일시/수정일시 등 감사 필드가 자동으로 채워지도록 한다.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {}
