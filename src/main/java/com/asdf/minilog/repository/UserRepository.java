package com.asdf.minilog.repository;

import com.asdf.minilog.entity.Role;
import com.asdf.minilog.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUserName(String username);

  @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
  List<User> findAllByRole(@Param("role") Role role);
}
