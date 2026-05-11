package com.asdf.minilog.security;

import com.asdf.minilog.entity.Role;
import org.springframework.security.core.GrantedAuthority;

public class MinilogGrantedAuthority implements GrantedAuthority {

  private final Role role;

  public MinilogGrantedAuthority(Role role) {
    this.role = role;
  }

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
