package com.asdf.minilog.repository.main;

import com.asdf.minilog.entity.main.Role;
import com.asdf.minilog.entity.main.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** 사용자(User) 엔티티를 관리하는 리포지토리. */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  /**
   * 사용자 이름으로 사용자를 조회한다. (로그인/인증 등에 사용)
   *
   * @param username 사용자 이름
   * @return 존재하면 사용자, 없으면 빈 Optional
   */
  Optional<User> findByUserName(String username);

  /**
   * 특정 권한(Role)을 가진 모든 사용자를 조회한다. User-roles 컬렉션을 JOIN하여 해당 권한 보유자를 가져온다.
   *
   * @param role 조회할 권한
   * @return 해당 권한을 가진 사용자 목록
   */
  @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
  List<User> findAllByRole(@Param("role") Role role);
}
