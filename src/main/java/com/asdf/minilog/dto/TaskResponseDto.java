package com.asdf.minilog.dto;

import com.asdf.minilog.entity.TaskStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * 작업(Task) 조회 응답 본문을 담는 DTO.
 *
 * <p>서버가 클라이언트에게 단일 작업 정보를 내려줄 때 사용하는 응답 페이로드이다. 소속 장비 ID와 상태, 생성/수정 시각을 포함한다.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskResponseDto {

  @NonNull private Long id;

  @NonNull private Long deviceId;

  @NonNull private String name;

  private String description;

  @NonNull private TaskStatus status;

  @NonNull private LocalDateTime createdAt;

  @NonNull private LocalDateTime updatedAt;
}
