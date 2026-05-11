package com.asdf.minilog.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceWithTasksResponseDto {

  @NonNull private Long id;

  @NonNull private String name;

  @NonNull private String type;

  @NonNull private LocalDateTime createdAt;

  @NonNull private LocalDateTime updatedAt;

  @NonNull private List<TaskSummaryDto> tasks;
}
