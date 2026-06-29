package com.asdf.minilog.entity.main;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 업로드된 첨부파일을 나타내는 엔티티. {@code attachments} 테이블에 매핑된다.
 *
 * <p>파일은 두 곳에 저장된다. 메타데이터와 파일 바이트({@code data})는 이 테이블에 저장되고, 동일한 바이트가 서버의 특정 폴더에 {@code
 * storedFileName} 이름으로 저장된다. 생성/수정 시각은 JPA Auditing으로 자동 관리된다.
 */
@Entity
@Table(name = "attachments")
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class Attachment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 업로드 당시의 원본 파일명. 다운로드 시 이 이름으로 내려준다. */
  @Column(name = "original_file_name", nullable = false)
  private String originalFileName;

  /** 서버 폴더에 저장될 때 사용하는 파일명(UUID 기반, 충돌 방지용). */
  @Column(name = "stored_file_name", nullable = false)
  private String storedFileName;

  /** 파일의 MIME 타입. 업로드 시 미지정이면 null일 수 있다. */
  @Column(name = "content_type")
  private String contentType;

  /** 파일 크기(바이트). */
  @Column(name = "file_size", nullable = false)
  private Long fileSize;

  /** DB에 저장되는 파일 바이트(LONGBLOB). 서버 폴더와 더불어 이중으로 저장된다. */
  @Lob
  @Column(name = "data", columnDefinition = "LONGBLOB", nullable = false)
  private byte[] data;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
