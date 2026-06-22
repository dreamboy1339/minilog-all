package com.asdf.minilog.batch.quake;

import static org.assertj.core.api.Assertions.assertThat;

import com.asdf.minilog.batch.quake.dto.UsgsFeature;
import com.asdf.minilog.batch.quake.dto.UsgsGeometry;
import com.asdf.minilog.batch.quake.dto.UsgsProperties;
import com.asdf.minilog.entity.quake.Earthquake;
import java.util.List;
import org.junit.jupiter.api.Test;

/** {@link QuakeBatchConfig}의 USGS Feature → Earthquake 변환 로직 단위 테스트. */
class QuakeBatchConfigTest {

  // processor()는 주입된 협력 객체를 사용하지 않으므로 null로 생성해도 무방하다.
  private final QuakeBatchConfig config = new QuakeBatchConfig(null, null);

  @Test
  void processor_convertsFeatureToEntity() throws Exception {
    UsgsFeature feature =
        new UsgsFeature(
            "us6000abcd",
            new UsgsProperties(
                5.2, "10 km NE of City", 1_700_000_000_000L, "https://example.test/eq", "mb"),
            // GeoJSON 좌표는 [경도, 위도, 깊이] 순서다.
            new UsgsGeometry(List.of(126.97, 37.56, 12.3)));

    Earthquake quake = config.quakeItemProcessor().process(feature);

    assertThat(quake).isNotNull();
    assertThat(quake.getId()).isEqualTo("us6000abcd");
    assertThat(quake.getMagnitude()).isEqualTo(5.2);
    assertThat(quake.getPlace()).isEqualTo("10 km NE of City");
    assertThat(quake.getLongitude()).isEqualTo(126.97);
    assertThat(quake.getLatitude()).isEqualTo(37.56);
    assertThat(quake.getDepth()).isEqualTo(12.3);
    assertThat(quake.getMagType()).isEqualTo("mb");
    assertThat(quake.getEventTime()).isNotNull();
  }

  @Test
  void processor_skipsFeatureWithoutMagnitude() throws Exception {
    UsgsFeature feature =
        new UsgsFeature(
            "us0001",
            new UsgsProperties(null, "somewhere", 1_700_000_000_000L, "u", "mb"),
            new UsgsGeometry(List.of(1.0, 2.0, 3.0)));

    assertThat(config.quakeItemProcessor().process(feature)).isNull();
  }

  @Test
  void processor_handlesMissingGeometry() throws Exception {
    UsgsFeature feature =
        new UsgsFeature(
            "us0002", new UsgsProperties(3.0, "place", 1_700_000_000_000L, "u", "ml"), null);

    Earthquake quake = config.quakeItemProcessor().process(feature);

    assertThat(quake).isNotNull();
    assertThat(quake.getLongitude()).isNull();
    assertThat(quake.getLatitude()).isNull();
    assertThat(quake.getDepth()).isNull();
  }
}
