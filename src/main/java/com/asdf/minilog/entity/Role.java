package com.asdf.minilog.entity;

/**
 * 사용자({@link User})의 권한을 나타내는 열거형.
 *
 * <p>Spring Security 규칙에 맞춰 {@code ROLE_} 접두사를 사용하며, 한 사용자가 여러 권한을 가질 수 있다.
 */
public enum Role {
  /** 시스템 관리자 권한. */
  ROLE_ADMIN,
  /** 작업 보고서 작성자 권한. */
  ROLE_AUTHOR,
  /** 작업 보고서 검토자 권한. */
  ROLE_REVIEWER,
  /** 작업 보고서 승인자 권한. */
  ROLE_APPROVER
}
