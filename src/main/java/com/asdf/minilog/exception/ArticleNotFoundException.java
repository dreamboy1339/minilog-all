package com.asdf.minilog.exception;

/**
 * 요청한 게시글(Article)을 찾을 수 없을 때 발생하는 예외입니다.
 *
 * <p>{@link GlobalExceptionHandler}에서 처리되어 HTTP 404 (Not Found) 응답으로 변환됩니다.
 */
public class ArticleNotFoundException extends RuntimeException {

  public ArticleNotFoundException(String message) {
    super(message);
  }
}
