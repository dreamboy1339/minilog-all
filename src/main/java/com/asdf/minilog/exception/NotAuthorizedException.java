package com.asdf.minilog.exception;

/**
 * 권한이 없는 사용자가 접근을 시도할 때 발생하는 예외입니다.
 *
 * <p>{@link GlobalExceptionHandler}에서 처리되어 HTTP 403 (Forbidden) 응답으로 변환됩니다.
 */
public class NotAuthorizedException extends RuntimeException {
  public NotAuthorizedException(String message) {
    super(message);
  }
}
