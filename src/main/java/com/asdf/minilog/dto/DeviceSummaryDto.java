package com.asdf.minilog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * 장비(Device) 요약 정보를 담는 DTO.
 *
 * <p>다른 응답에 장비 정보를 중첩해서 내려줄 때 사용하는 경량 투영(projection)이다. 식별자와 이름, 종류만 포함한다.
 *
 * @see TaskWithDeviceResponseDto
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceSummaryDto {

  @NonNull private Long id;

  @NonNull private String name;

  @NonNull private String type;
}
