package com.asdf.minilog.exception;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 애플리케이션 전역에서 발생하는 예외를 한곳에서 처리하는 {@code @ControllerAdvice}입니다.
 *
 * <p>각 핸들러 메서드가 예외를 적절한 HTTP 상태 코드와 응답 본문으로 변환합니다.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /** {@link UserNotFoundException}를 처리하여 HTTP 404 (Not Found) 응답을 반환합니다. */
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException exception) {
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
  }

  /** {@link ArticleNotFoundException}를 처리하여 HTTP 404 (Not Found) 응답을 반환합니다. */
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "404", description = "Article not found"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @ExceptionHandler(ArticleNotFoundException.class)
  public ResponseEntity<String> handleArticleNotFoundException(ArticleNotFoundException exception) {
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
  }

  /** {@link DeviceNotFoundException}를 처리하여 HTTP 404 (Not Found) 응답을 반환합니다. */
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "404", description = "Device not found"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @ExceptionHandler(DeviceNotFoundException.class)
  public ResponseEntity<String> handleDeviceNotFoundException(DeviceNotFoundException exception) {
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
  }

  /** {@link TaskNotFoundException}를 처리하여 HTTP 404 (Not Found) 응답을 반환합니다. */
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @ExceptionHandler(TaskNotFoundException.class)
  public ResponseEntity<String> handleTaskNotFoundException(TaskNotFoundException exception) {
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
  }

  /** {@link TaskReportNotFoundException}를 처리하여 HTTP 404 (Not Found) 응답을 반환합니다. */
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "404", description = "Task report not found"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @ExceptionHandler(TaskReportNotFoundException.class)
  public ResponseEntity<String> handleTaskReportNotFoundException(
      TaskReportNotFoundException exception) {
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
  }

  /** {@link AttachmentNotFoundException}를 처리하여 HTTP 404 (Not Found) 응답을 반환합니다. */
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "404", description = "Attachment not found"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @ExceptionHandler(AttachmentNotFoundException.class)
  public ResponseEntity<String> handleAttachmentNotFoundException(
      AttachmentNotFoundException exception) {
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
  }

  /** {@link NotAuthorizedException}를 처리하여 HTTP 403 (Forbidden) 응답을 반환합니다. */
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @ExceptionHandler(NotAuthorizedException.class)
  public ResponseEntity<String> handleNotAuthorizedException(NotAuthorizedException exception) {
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.FORBIDDEN);
  }

  /** {@link IllegalArgumentException}를 처리하여 HTTP 400 (Bad Request) 응답을 반환합니다. */
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException exception) {
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
  }

  /** 위에서 처리되지 않은 모든 예외를 처리하여 로그를 남기고 HTTP 500 (Internal Server Error) 응답을 반환합니다. */
  @ApiResponses(value = {@ApiResponse(responseCode = "500", description = "Internal server error")})
  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handleException(Exception exception) {
    logger.error("Unhandled exception", exception);
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
