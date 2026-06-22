package com.asdf.minilog.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 작업 보고서(TaskReport) 생성 요청 본문을 담는 DTO.
 *
 * <p>특정 작업에 대한 보고서를 처음 작성할 때 서버로 전달하는 요청 페이로드이다.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskReportRequestDto {

  /** 보고서가 속한 작업 ID (필수). */
  @NotNull private Long taskId;

  /** 보고서 내용. */
  private String content;
}
