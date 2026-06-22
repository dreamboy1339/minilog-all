package com.asdf.minilog.dto;

import com.asdf.minilog.entity.task.TaskStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * 작업(Task)과 소속 장비(Device) 요약 정보를 함께 담는 조회 응답 DTO.
 *
 * <p>작업 상세 정보와 해당 작업이 속한 장비 정보를 한 번에 내려줄 때 사용하는 응답 페이로드이다.
 *
 * @see DeviceSummaryDto
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskWithDeviceResponseDto {

  @NonNull private Long id;

  @NonNull private String name;

  private String description;

  @NonNull private TaskStatus status;

  @NonNull private LocalDateTime createdAt;

  @NonNull private LocalDateTime updatedAt;

  @NonNull private DeviceSummaryDto device;
}
