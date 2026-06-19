package com.asdf.minilog.entity.main;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 서비스 사용자를 나타내는 엔티티. {@code users} 테이블에 매핑된다.
 *
 * <p>여러 권한({@link Role})과 여러 게시글({@link Article})을 가진다(1:N). 비밀번호는 저장 시 BCrypt로 암호화되며, 생성/수정 시각은
 * JPA Auditing으로 자동 관리된다.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
@AllArgsConstructor
@Data
@NoArgsConstructor
public class User {

  /** 비밀번호 암호화에 사용하는 BCrypt 인코더. */
  private static PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 로그인 ID. {@code username} 컬럼에 매핑되며 유니크하다. */
  @Column(nullable = false, unique = true, name = "username")
  private String userName;

  /** BCrypt로 암호화되어 저장되는 비밀번호. */
  @Column(nullable = false)
  private String password;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  /** 사용자가 가진 권한 목록. 별도의 {@code user_roles} 테이블에 저장된다. */
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "role")
  private Set<Role> roles;

  /** 사용자가 작성한 게시글 목록. 사용자 삭제 시 함께 삭제된다(1:N, cascade, orphanRemoval). */
  @OneToMany(
      mappedBy = "author",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<Article> articles;

  public static UserBuilder builder() {
    return new UserBuilder();
  }

  /** 비밀번호를 BCrypt로 암호화하여 저장한다. */
  public void setPassword(String password) {
    this.password = passwordEncoder.encode(password);
  }

  public static class UserBuilder {
    private Long id;
    private String userName;
    private String password;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Article> articles;
    private static PasswordEncoder passwordEncoder = User.passwordEncoder;
    private Set<Role> roles;

    public UserBuilder id(Long id) {
      this.id = id;
      return this;
    }

    public UserBuilder userName(String userName) {
      this.userName = userName;
      return this;
    }

    public UserBuilder password(String password) {
      this.password = passwordEncoder.encode(password);
      return this;
    }

    public UserBuilder createdAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public UserBuilder updatedAt(LocalDateTime updatedAt) {
      this.updatedAt = updatedAt;
      return this;
    }

    public UserBuilder articles(List<Article> articles) {
      this.articles = articles;
      return this;
    }

    public UserBuilder roles(Set<Role> roles) {
      this.roles = roles;
      return this;
    }

    public User build() {
      return new User(id, userName, password, createdAt, updatedAt, roles, articles);
    }
  }
}
