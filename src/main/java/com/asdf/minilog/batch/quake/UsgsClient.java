package com.asdf.minilog.batch.quake;

import com.asdf.minilog.batch.quake.dto.UsgsFeature;
import com.asdf.minilog.batch.quake.dto.UsgsFeatureCollection;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** USGS 지진 카탈로그(FDSN Event) API 호출을 담당하는 클라이언트. */
@Component
public class UsgsClient {

  /** 수집 대상 최소 규모. */
  private static final String MIN_MAGNITUDE = "2.5";

  private final RestClient usgsRestClient;

  public UsgsClient(@Qualifier("usgsRestClient") RestClient usgsRestClient) {
    this.usgsRestClient = usgsRestClient;
  }

  /**
   * 주어진 구간의 지진 이벤트를 조회한다.
   *
   * @param startTime ISO-8601 시작 시각(UTC)
   * @param endTime ISO-8601 종료 시각(UTC)
   * @return 지진 Feature 목록(없으면 빈 목록)
   */
  public List<UsgsFeature> fetchEarthquakes(String startTime, String endTime) {
    UsgsFeatureCollection response =
        usgsRestClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path("/query")
                        .queryParam("format", "geojson")
                        .queryParam("starttime", startTime)
                        .queryParam("endtime", endTime)
                        .queryParam("minmagnitude", MIN_MAGNITUDE)
                        .build())
            .retrieve()
            .body(UsgsFeatureCollection.class);
    return response == null || response.features() == null
        ? Collections.emptyList()
        : response.features();
  }
}
