package com.asdf.minilog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 로그인(인증) 요청 본문을 담는 DTO.
 *
 * <p>사용자가 아이디와 비밀번호로 로그인할 때 서버로 전달하는 요청 페이로드이다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationRequestDto {
  private String username;
  private String password;
}
