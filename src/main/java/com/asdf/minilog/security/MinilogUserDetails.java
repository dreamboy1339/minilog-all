package com.asdf.minilog.security;

import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Spring Security 인증에 사용되는 사용자 정보 구현체.
 *
 * <p>사용자 ID/이름/비밀번호와 권한 목록을 담아 {@link UserDetails} 계약을 충족한다. Lombok {@code @Data}가 getter
 * 등을 생성한다.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MinilogUserDetails implements UserDetails {
  private Long id;
  private String username;
  private String password;
  private Collection<? extends GrantedAuthority> authorities;
}
