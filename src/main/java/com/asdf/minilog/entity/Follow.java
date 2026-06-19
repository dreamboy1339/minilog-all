package com.asdf.minilog.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 사용자 간 팔로우 관계를 나타내는 엔티티. {@code follows} 테이블에 매핑된다.
 *
 * <p>{@link User}를 양쪽으로 참조하는 자기 참조 관계이며, (follower, followee) 조합은 유니크 제약으로 중복 팔로우를 막는다.
 */
@Entity
@Table(
    name = "follows",
    indexes = {
      @Index(name = "idx_follower_id", columnList = "follower_id"),
      @Index(name = "idx_followee_id", columnList = "followee_id")
    },
    uniqueConstraints = {@UniqueConstraint(columnNames = {"follower_id", "followee_id"})})
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class Follow {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 팔로우를 하는 사용자(팔로워). */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "follower_id", nullable = false)
  private User follower;

  /** 팔로우를 받는 사용자(팔로위). */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "followee_id", nullable = false)
  private User followee;
}
