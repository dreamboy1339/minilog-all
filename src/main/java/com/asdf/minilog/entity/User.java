package com.asdf.minilog.entity;

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

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
@AllArgsConstructor
@Data
@NoArgsConstructor
public class User {

  private static PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, name = "username")
  private String userName;

  @Column(nullable = false)
  private String password;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "role")
  private Set<Role> roles;

  @OneToMany(
      mappedBy = "author",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<Article> articles;

  public static UserBuilder builder() {
    return new UserBuilder();
  }

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
