package com.asdf.minilog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 장비(Device) 생성/수정 요청 본문을 담는 DTO.
 *
 * <p>클라이언트가 장비를 등록하거나 수정할 때 서버로 전달하는 요청 페이로드이다.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceRequestDto {

  /** 장비 이름 (필수, 공백 불가). */
  @NotBlank private String name;

  /** 장비 종류 (필수, 공백 불가). */
  @NotBlank private String type;
}
