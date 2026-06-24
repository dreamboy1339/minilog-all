package com.asdf.minilog.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 스케줄링 활성화 설정 클래스.
 *
 * <p>{@code @EnableScheduling}으로 {@code @Scheduled} 메서드(시세 수집 스케줄러, 지진 배치 트리거)가 동작하도록 한다.
 * {@code app.scheduling.enabled=false}로 비활성화할 수 있다(테스트에서 외부 API 호출을 막는 용도).
 */
@Configuration
@ConditionalOnProperty(
    prefix = "app.scheduling",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@EnableScheduling
public class SchedulingConfig {}
