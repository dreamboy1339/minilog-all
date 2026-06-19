package com.asdf.minilog.security;

import com.asdf.minilog.entity.Role;
import org.springframework.security.core.GrantedAuthority;

/**
 * 애플리케이션의 {@link Role}을 Spring Security의 {@link GrantedAuthority}로 표현하는 권한 객체.
 *
 * <p>권한 문자열로 역할 이름({@code Role.name()})을 사용하며, 동일한 역할이면 같은 권한으로 취급되도록 equals/hashCode를 재정의한다.
 */
public class MinilogGrantedAuthority implements GrantedAuthority {

  private final Role role;

  public MinilogGrantedAuthority(Role role) {
    this.role = role;
  }

  /**
   * 권한 문자열로 역할 이름을 반환한다.
   *
   * @return 역할 이름(예: {@code ROLE_ADMIN})
   */
  @Override
  public String getAuthority() {
    return role.name();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof MinilogGrantedAuthority) {
      return role.equals(((MinilogGrantedAuthority) o).role);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return role.hashCode();
  }

  @Override
  public String toString() {
    return this.role.name();
  }
}
