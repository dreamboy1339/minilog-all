package com.asdf.minilog.config;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * 외부 공개 API 호출용 {@link RestClient} 빈 구성.
 *
 * <p>USGS 지진 카탈로그와 CoinGecko 시세 API를 호출하는 두 개의 RestClient를 제공한다. 두 빈 모두 동일한 연결/읽기 타임아웃을 사용하며, 주입 시
 * {@code @Qualifier}로 구분한다.
 */
@Configuration
public class RestClientConfig {

  private static SimpleClientHttpRequestFactory timeoutRequestFactory() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(Duration.ofSeconds(5));
    factory.setReadTimeout(Duration.ofSeconds(15));
    return factory;
  }

  /** USGS 지진 카탈로그 API용 RestClient. */
  @Bean
  public RestClient usgsRestClient() {
    return RestClient.builder()
        .baseUrl("https://earthquake.usgs.gov/fdsnws/event/1")
        .requestFactory(timeoutRequestFactory())
        .build();
  }

  /** CoinGecko 시세 API용 RestClient. */
  @Bean
  public RestClient coinGeckoRestClient() {
    return RestClient.builder()
        .baseUrl("https://api.coingecko.com/api/v3")
        .requestFactory(timeoutRequestFactory())
        .build();
  }
}
