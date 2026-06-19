package com.asdf.minilog.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * 장비(Device)와 해당 장비에 속한 작업(Task) 목록을 함께 담는 조회 응답 DTO.
 *
 * <p>장비 상세 정보와 연관된 작업들을 한 번에 내려줄 때 사용하는 응답 페이로드이다.
 *
 * @see TaskSummaryDto
 */
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
