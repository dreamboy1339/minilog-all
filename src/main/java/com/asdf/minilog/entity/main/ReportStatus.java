package com.asdf.minilog.entity.main;

/**
 * 작업 보고서({@link TaskReport})의 처리 상태를 나타내는 열거형.
 *
 * <p>작성(DRAFT) → 제출(SUBMITTED) → 검토(REVIEW) → 승인 요청(APPROVAL) → 승인 완료(APPROVED) 흐름을 따른다.
 */
public enum ReportStatus {
  /** 작성 중인 임시 저장 상태. */
  DRAFT,
  /** 검토를 위해 제출된 상태. */
  SUBMITTED,
  /** 검토자가 검토 중인 상태. */
  REVIEW,
  /** 승인 대기 상태. */
  APPROVAL,
  /** 승인이 완료된 상태. */
  APPROVED
}
