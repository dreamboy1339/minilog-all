package com.asdf.minilog.dto;

import com.asdf.minilog.entity.main.Role;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * 사용자(User) 조회 응답 본문을 담는 DTO.
 *
 * <p>서버가 사용자 정보를 내려줄 때 사용하는 응답 페이로드이다. 비밀번호는 제외하고 식별자, 아이디, 보유 권한({@link
 * com.asdf.minilog.entity.main.Role})만 포함한다.
 */
@Data
@Builder
public class UserResponseDto {

  @NonNull private Long id;

  @NonNull private String username;

  private Set<Role> roles;
}
