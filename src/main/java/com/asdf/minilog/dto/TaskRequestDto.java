package com.asdf.minilog.dto;

import com.asdf.minilog.entity.task.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 작업(Task) 생성/수정 요청 본문을 담는 DTO.
 *
 * <p>클라이언트가 특정 장비에 속한 작업을 등록하거나 수정할 때 서버로 전달하는 요청 페이로드이다.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskRequestDto {

  /** 작업이 속한 장비 ID (필수). */
  @NotNull private Long deviceId;

  /** 작업 이름 (필수, 공백 불가, 최대 255자). */
  @NotBlank
  @Size(max = 255)
  private String name;

  /** 작업 설명 (선택, 최대 1000자). */
  @Size(max = 1000)
  private String description;

  /** 작업 상태. 생략 시 서버 기본값이 적용된다. */
  private TaskStatus status;
}
