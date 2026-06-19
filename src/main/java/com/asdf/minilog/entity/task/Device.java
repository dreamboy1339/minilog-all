package com.asdf.minilog.entity.task;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 작업({@link Task})이 수행되는 장비를 나타내는 엔티티. {@code devices} 테이블에 매핑된다.
 *
 * <p>하나의 Device는 여러 개의 Task를 가진다(1:N). 생성/수정 시각은 JPA Auditing으로 자동 관리된다.
 */
@Entity
@Table(name = "devices")
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class Device {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String type;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  /** 이 장비에 속한 작업 목록. 장비 삭제 시 함께 삭제된다(cascade, orphanRemoval). */
  @ToString.Exclude
  @Builder.Default
  @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Task> tasks = new ArrayList<>();
}
