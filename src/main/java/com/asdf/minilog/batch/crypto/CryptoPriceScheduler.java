package com.asdf.minilog.batch.crypto;

import com.asdf.minilog.entity.crypto.CryptoPrice;
import com.asdf.minilog.repository.crypto.CryptoPriceRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

/**
 * CoinGecko 시세를 주기적으로 수집해 crypto_db에 적재하는 스케줄러.
 *
 * <p>설정된 주기(기본 3분)마다 지정 코인의 USD/KRW 가격을 조회하여 코인별로 1건씩 저장한다. crypto_db에 쓰므로
 * {@code @Transactional("cryptoTransactionManager")}로 트랜잭션 매니저를 명시한다.
 */
@Component
public class CryptoPriceScheduler {

  private static final List<String> COINS = List.of("bitcoin", "ethereum");

  private static final ParameterizedTypeReference<Map<String, Map<String, BigDecimal>>>
      RESPONSE_TYPE = new ParameterizedTypeReference<>() {};

  private final RestClient coinGeckoRestClient;
  private final CryptoPriceRepository cryptoPriceRepository;

  public CryptoPriceScheduler(
      @Qualifier("coinGeckoRestClient") RestClient coinGeckoRestClient,
      CryptoPriceRepository cryptoPriceRepository) {
    this.coinGeckoRestClient = coinGeckoRestClient;
    this.cryptoPriceRepository = cryptoPriceRepository;
  }

  /** 설정된 주기(기본 3분)마다 시세를 수집·적재한다. */
  @Scheduled(fixedDelayString = "${crypto.batch.interval-ms:180000}")
  @Transactional("cryptoTransactionManager")
  public void collectPrices() {
    Map<String, Map<String, BigDecimal>> prices =
        coinGeckoRestClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path("/simple/price")
                        .queryParam("ids", String.join(",", COINS))
                        .queryParam("vs_currencies", "usd,krw")
                        .build())
            .retrieve()
            .body(RESPONSE_TYPE);
    if (prices == null) {
      return;
    }
    List<CryptoPrice> snapshots = new ArrayList<>();
    for (String coin : COINS) {
      Map<String, BigDecimal> price = prices.get(coin);
      if (price == null) {
        continue;
      }
      snapshots.add(
          CryptoPrice.builder()
              .coin(coin)
              .priceUsd(price.get("usd"))
              .priceKrw(price.get("krw"))
              .build());
    }
    cryptoPriceRepository.saveAll(snapshots);
  }
}
