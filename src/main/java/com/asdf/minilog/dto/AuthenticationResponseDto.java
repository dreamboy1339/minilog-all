package com.asdf.minilog.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * 로그인(인증) 성공 응답 본문을 담는 DTO.
 *
 * <p>인증에 성공하면 발급된 JWT 토큰을 클라이언트에게 내려준다.
 */
@Data
@Builder
public class AuthenticationResponseDto {
  /** 발급된 JWT 인증 토큰. */
  @NonNull private String jwt;
}
