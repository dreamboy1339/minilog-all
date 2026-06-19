package com.asdf.minilog.dto;

import com.asdf.minilog.entity.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * 작업(Task) 요약 정보를 담는 DTO.
 *
 * <p>장비 응답 등에 작업 목록을 중첩해서 내려줄 때 사용하는 경량 투영(projection)이다.
 *
 * @see DeviceWithTasksResponseDto
 */
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
