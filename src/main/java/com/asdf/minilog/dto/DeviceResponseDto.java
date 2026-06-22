package com.asdf.minilog.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * 장비(Device) 조회 응답 본문을 담는 DTO.
 *
 * <p>서버가 클라이언트에게 단일 장비 정보를 내려줄 때 사용하는 응답 페이로드이다. 생성/수정 시각을 포함한다.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceResponseDto {

  @NonNull private Long id;

  @NonNull private String name;

  @NonNull private String type;

  @NonNull private LocalDateTime createdAt;

  @NonNull private LocalDateTime updatedAt;
}
