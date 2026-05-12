package com.asdf.minilog.dto;

import com.asdf.minilog.entity.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskSummaryDto {

  @NonNull private Long id;

  @NonNull private String name;

  private String description;

  private TaskStatus status;
}
