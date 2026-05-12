package com.asdf.minilog.dto;

import com.asdf.minilog.entity.Role;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class UserResponseDto {

  @NonNull private Long id;

  @NonNull private String username;

  private Set<Role> roles;
}
