package com.asdf.minilog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * 첨부파일 업로드 결과(메타데이터)를 담는 응답 DTO.
 *
 * <p>업로드된 파일의 식별자와 원본 파일명, 타입, 크기 등을 클라이언트에 반환한다. 다운로드 시에는 이 {@code id}를 사용한다.
 */
@Data
@Builder
public class AttachmentResponseDto {

  /** 첨부파일 식별자. 다운로드 API에서 이 값을 사용한다. */
  @NonNull private Long id;

  /** 업로드 당시의 원본 파일명. */
  @NonNull private String originalFileName;

  /** 파일의 MIME 타입. 업로드 시 미지정이면 null일 수 있다. */
  @Schema(description = "MIME type of the uploaded file", example = "image/png")
  private String contentType;

  /** 파일 크기(바이트). */
  @NonNull private Long fileSize;

  /** 업로드(생성) 시각. */
  @NonNull private LocalDateTime createdAt;
}
