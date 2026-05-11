package com.asdf.minilog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceSummaryDto {

  @NonNull private Long id;

  @NonNull private String name;

  @NonNull private String type;
}
