package com.asdf.minilog.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskReportSubmitRequestDto {

  @NotNull private Long reviewerId;

  @NotNull private Long approverId;
}
