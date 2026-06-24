package com.asdf.minilog.entity.quake;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * USGS 지진 카탈로그의 단일 지진 이벤트를 나타내는 엔티티. {@code earthquakes} 테이블에 매핑된다.
 *
 * <p>USGS가 부여한 이벤트 id를 자연키(PK)로 사용한다. 동일 이벤트가 사후 보정되어 재수집될 수 있으므로 {@code save()} 시 기존 행이 갱신(업서트)된다.
 * 따라서 다른 엔티티와 달리 {@code @GeneratedValue}를 쓰지 않는다. 마지막 수집/갱신 시각은 JPA Auditing으로 자동 관리된다.
 */
@Entity
@Table(name = "earthquakes")
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class Earthquake {

  /** USGS 이벤트 id(자연키). 예: "us6000abcd". */
  @Id private String id;

  /** 규모(magnitude). */
  private Double magnitude;

  /** 진앙 지역 설명. 예: "10 km NE of Some City". */
  @Column(length = 512)
  private String place;

  /** 지진 발생 시각(UTC). USGS {@code properties.time}(epoch milliseconds)에서 변환한다. */
  @Column(name = "event_time")
  private LocalDateTime eventTime;

  /** 경도(longitude). */
  private Double longitude;

  /** 위도(latitude). */
  private Double latitude;

  /** 진원 깊이(km). */
  private Double depth;

  /** 규모 산출 방식. 예: "mb", "ml", "mww". */
  @Column(name = "mag_type")
  private String magType;

  /** USGS 상세 페이지 URL. */
  @Column(length = 512)
  private String url;

  /** 마지막 수집/갱신 시각. */
  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
