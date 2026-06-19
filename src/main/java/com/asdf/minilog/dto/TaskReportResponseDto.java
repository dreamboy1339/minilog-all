package com.asdf.minilog.dto;

import com.asdf.minilog.entity.ReportStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * 작업 보고서(TaskReport) 조회 응답 본문을 담는 DTO.
 *
 * <p>서버가 보고서 상세 정보를 내려줄 때 사용하는 응답 페이로드이다. 작성자/검토자/승인자 정보와 결재 상태({@link
 * com.asdf.minilog.entity.ReportStatus})를 포함한다.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskReportResponseDto {

  @NonNull private Long id;

  @NonNull private Long taskId;

  @NonNull private Long authorId;

  @NonNull private String authorName;

  private Long reviewerId;

  private String reviewerName;

  private Long approverId;

  private String approverName;

  private String content;

  /** 보고서 결재 상태(작성/제출/검토/승인 등). */
  @NonNull private ReportStatus status;

  @NonNull private LocalDateTime createdAt;

  @NonNull private LocalDateTime updatedAt;
}
