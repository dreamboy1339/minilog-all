package com.asdf.minilog.entity.task;

/**
 * 작업({@link Task})의 진행 상태를 나타내는 열거형.
 *
 * <p>STARTED → IN_PROGRESS → COMPLETED 순으로 진행된다.
 */
public enum TaskStatus {
  /** 작업이 시작된 초기 상태(기본값). */
  STARTED,
  /** 작업이 진행 중인 상태. */
  IN_PROGRESS,
  /** 작업이 완료된 상태. */
  COMPLETED
}
