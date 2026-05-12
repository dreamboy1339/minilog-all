package com.asdf.minilog.dto;

import com.asdf.minilog.entity.ReportStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

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

  @NonNull private ReportStatus status;

  @NonNull private LocalDateTime createdAt;

  @NonNull private LocalDateTime updatedAt;
}
