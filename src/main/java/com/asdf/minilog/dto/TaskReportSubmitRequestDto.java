package com.asdf.minilog.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 작업 보고서(TaskReport) 제출(결재 상신) 요청 본문을 담는 DTO.
 *
 * <p>작성된 보고서를 결재 흐름에 올릴 때 검토자와 승인자를 지정하기 위한 요청 페이로드이다.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskReportSubmitRequestDto {

  /** 보고서를 검토할 검토자 ID (필수). */
  @NotNull private Long reviewerId;

  /** 보고서를 최종 승인할 승인자 ID (필수). */
  @NotNull private Long approverId;
}
