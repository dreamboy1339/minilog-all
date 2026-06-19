package com.asdf.minilog.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * 사용자(User) 가입/등록 요청 본문을 담는 DTO.
 *
 * <p>새 사용자를 생성할 때 아이디와 비밀번호를 서버로 전달하는 요청 페이로드이다.
 */
@Data
@Builder
public class UserRequestDto {

  /** 사용자 아이디 (필수). */
  @NonNull private String username;

  /** 사용자 비밀번호 (필수). */
  @NonNull private String password;
}
