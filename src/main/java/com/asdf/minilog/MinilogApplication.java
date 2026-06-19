package com.asdf.minilog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Minilog 애플리케이션의 스프링 부트 진입점.
 *
 * <p>{@code @SpringBootApplication}으로 컴포넌트 스캔, 자동 설정, 설정 클래스를 활성화한다.
 */
@SpringBootApplication
public class MinilogApplication {

  /**
   * 애플리케이션을 부팅한다.
   *
   * @param args 실행 인자
   */
  public static void main(String[] args) {
    SpringApplication.run(MinilogApplication.class, args);
  }
}
