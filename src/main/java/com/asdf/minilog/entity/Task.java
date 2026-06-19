package com.asdf.minilog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 장비({@link Device})에서 수행되는 작업을 나타내는 엔티티. {@code tasks} 테이블에 매핑된다.
 *
 * <p>하나의 Task는 하나의 Device에 속한다(N:1). 진행 상태는 {@link TaskStatus}로 관리되며, 생성/수정 시각은 JPA Auditing으로 자동
 * 관리된다.
 */
@Entity
@Table(name = "tasks")
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class Task {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 이 작업이 속한 장비. 여러 작업이 한 장비에 속한다(N:1). */
  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "device_id", nullable = false)
  private Device device;

  @Column(nullable = false, length = 255)
  private String name;

  @Column(length = 1000)
  private String description;

  /** 작업 진행 상태. 기본값은 {@link TaskStatus#STARTED}. */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  @Builder.Default
  private TaskStatus status = TaskStatus.STARTED;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
