package com.asdf.minilog.entity.crypto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 특정 시점의 암호화폐 시세 스냅샷을 나타내는 엔티티. {@code crypto_prices} 테이블에 매핑된다.
 *
 * <p>스케줄러가 주기적으로 코인별 1건씩 적재하여 시세 시계열을 누적한다. 적재 시각은 JPA Auditing으로 자동 관리된다.
 */
@Entity
@Table(name = "crypto_prices")
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class CryptoPrice {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 코인 식별자. 예: "bitcoin", "ethereum". */
  @Column(nullable = false)
  private String coin;

  /** 미국 달러(USD) 가격. */
  @Column(name = "price_usd", precision = 20, scale = 8)
  private BigDecimal priceUsd;

  /** 대한민국 원(KRW) 가격. */
  @Column(name = "price_krw", precision = 20, scale = 8)
  private BigDecimal priceKrw;

  /** 시세 조회/적재 시각. */
  @CreatedDate
  @Column(name = "fetched_at", nullable = false, updatable = false)
  private LocalDateTime fetchedAt;
}
