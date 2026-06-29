package com.asdf.minilog.exception;

/**
 * 요청한 첨부파일(Attachment)을 찾을 수 없을 때 발생하는 예외입니다.
 *
 * <p>{@link GlobalExceptionHandler}에서 처리되어 HTTP 404 (Not Found) 응답으로 변환됩니다.
 */
public class AttachmentNotFoundException extends RuntimeException {

  public AttachmentNotFoundException(String message) {
    super(message);
  }
}
