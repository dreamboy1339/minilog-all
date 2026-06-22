package com.asdf.minilog.entity.main;

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
 * 작업(Task)에 대한 보고서를 나타내는 엔티티. {@code task_reports} 테이블에 매핑된다.
 *
 * <p>하나의 작업당 하나의 보고서를 가진다. 작업은 task_db에 있으므로 {@code taskId}로만 참조한다. 작성자/검토자/승인자는 각각 {@link User}를
 * 참조하며(N:1, 검토자와 승인자는 선택적), 결재 진행 상태는 {@link ReportStatus}로 관리된다. 생성/수정 시각은 JPA Auditing으로 자동 관리된다.
 */
@Entity
@Table(name = "task_reports")
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class TaskReport {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 보고 대상 작업 ID. 작업당 보고서는 하나만 존재한다(unique). 작업은 task_db에 있어 ID로만 참조한다. */
  @Column(name = "task_id", nullable = false, unique = true)
  private Long taskId;

  /** 보고서 작성자(필수). */
  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false)
  private User author;

  /** 보고서 검토자(선택). */
  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reviewer_id")
  private User reviewer;

  /** 보고서 승인자(선택). */
  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "approver_id")
  private User approver;

  /** 보고서 본문(TEXT 타입). */
  @Column(columnDefinition = "TEXT")
  private String content;

  /** 보고서 결재 진행 상태. */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private ReportStatus status;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
