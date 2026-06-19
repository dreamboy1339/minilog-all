package com.asdf.minilog.service;

import com.asdf.minilog.entity.User;
import com.asdf.minilog.repository.UserRepository;
import com.asdf.minilog.security.MinilogGrantedAuthority;
import com.asdf.minilog.security.MinilogUserDetails;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security 인증에 사용되는 {@link UserDetailsService} 구현체.
 *
 * <p>사용자명으로 사용자를 조회하고, 해당 사용자의 권한(Role)을 {@link MinilogGrantedAuthority}로 변환하여 인증에 필요한 {@link
 * UserDetails}를 제공한다.
 */
@Service
public class MinilogUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Autowired
  public MinilogUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * 사용자명으로 사용자를 조회하여 인증용 {@link UserDetails}를 생성한다.
   *
   * @param username 조회할 사용자명
   * @return 사용자 정보와 권한이 담긴 {@link UserDetails}
   * @throws UsernameNotFoundException 해당 사용자명을 가진 사용자가 없는 경우
   */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user =
        userRepository
            .findByUserName(username)
            .orElseThrow(
                () -> new UsernameNotFoundException("User not found with username: " + username));

    List<GrantedAuthority> authorities =
        user.getRoles().stream().map(MinilogGrantedAuthority::new).collect(Collectors.toList());

    return new MinilogUserDetails(user.getId(), username, user.getPassword(), authorities);
  }
}
