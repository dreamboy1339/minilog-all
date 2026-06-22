package com.asdf.minilog.exception;

/**
 * 요청한 장치(Device)를 찾을 수 없을 때 발생하는 예외입니다.
 *
 * <p>{@link GlobalExceptionHandler}에서 처리되어 HTTP 404 (Not Found) 응답으로 변환됩니다.
 */
public class DeviceNotFoundException extends RuntimeException {

  public DeviceNotFoundException(String message) {
    super(message);
  }
}
