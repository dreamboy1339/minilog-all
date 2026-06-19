package com.asdf.minilog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 작업 보고서(TaskReport) 수정 요청 본문을 담는 DTO.
 *
 * <p>이미 작성된 보고서의 내용을 변경할 때 서버로 전달하는 요청 페이로드이다.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskReportUpdateRequestDto {

  /** 수정할 보고서 내용. */
  private String content;
}
