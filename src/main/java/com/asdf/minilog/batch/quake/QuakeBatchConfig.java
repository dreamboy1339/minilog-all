package com.asdf.minilog.batch.quake;

import com.asdf.minilog.batch.quake.dto.UsgsFeature;
import com.asdf.minilog.batch.quake.dto.UsgsProperties;
import com.asdf.minilog.entity.quake.Earthquake;
import com.asdf.minilog.repository.quake.EarthquakeRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 지진 수집 배치 구성. USGS 카탈로그를 증분 윈도우로 조회하여 청크 단위로 quake_db에 업서트한다.
 *
 * <p>Reader가 윈도우 구간의 이벤트를 한 번에 조회해 하나씩 공급하고, Processor가 엔티티로 변환하며, Writer가 {@code saveAll}로 업서트한다.
 * 청크 트랜잭션은 적재 대상 데이터소스의 {@code quakeTransactionManager}를 사용한다(배치 메타데이터는 기본 데이터소스를 사용함에 유의).
 */
@Configuration
public class QuakeBatchConfig {

  /** 한 번에 처리/적재할 청크 크기. */
  private static final int CHUNK_SIZE = 100;

  private final UsgsClient usgsClient;
  private final EarthquakeRepository earthquakeRepository;

  public QuakeBatchConfig(UsgsClient usgsClient, EarthquakeRepository earthquakeRepository) {
    this.usgsClient = usgsClient;
    this.earthquakeRepository = earthquakeRepository;
  }

  /** 윈도우 구간의 지진을 조회해 하나씩 공급하는 Reader. 구간은 JobParameter로 주입된다. */
  @Bean
  @StepScope
  public ItemReader<UsgsFeature> quakeItemReader(
      @Value("#{jobParameters['startTime']}") String startTime,
      @Value("#{jobParameters['endTime']}") String endTime) {
    List<UsgsFeature> features = usgsClient.fetchEarthquakes(startTime, endTime);
    return new IteratorItemReader<>(features);
  }

  /** USGS Feature를 Earthquake 엔티티로 변환한다. 규모가 없는 이벤트는 적재에서 제외한다. */
  @Bean
  public ItemProcessor<UsgsFeature, Earthquake> quakeItemProcessor() {
    return feature -> {
      if (feature.id() == null
          || feature.properties() == null
          || feature.properties().mag() == null) {
        return null;
      }
      UsgsProperties p = feature.properties();
      List<Double> coords = feature.geometry() != null ? feature.geometry().coordinates() : null;
      Double longitude = coords != null && coords.size() > 0 ? coords.get(0) : null;
      Double latitude = coords != null && coords.size() > 1 ? coords.get(1) : null;
      Double depth = coords != null && coords.size() > 2 ? coords.get(2) : null;
      LocalDateTime eventTime =
          p.time() != null
              ? LocalDateTime.ofInstant(Instant.ofEpochMilli(p.time()), ZoneOffset.UTC)
              : null;
      return Earthquake.builder()
          .id(feature.id())
          .magnitude(p.mag())
          .place(p.place())
          .eventTime(eventTime)
          .longitude(longitude)
          .latitude(latitude)
          .depth(depth)
          .magType(p.magType())
          .url(p.url())
          .build();
    };
  }

  /** 변환된 엔티티를 quake_db에 업서트한다. */
  @Bean
  public ItemWriter<Earthquake> quakeItemWriter() {
    return chunk -> earthquakeRepository.saveAll(chunk.getItems());
  }

  /** 지진 수집 Step. 청크 트랜잭션은 quakeTransactionManager를 사용한다. */
  @Bean
  public Step quakeIngestStep(
      JobRepository jobRepository,
      @Qualifier("quakeTransactionManager") PlatformTransactionManager quakeTransactionManager,
      ItemReader<UsgsFeature> quakeItemReader,
      ItemProcessor<UsgsFeature, Earthquake> quakeItemProcessor,
      ItemWriter<Earthquake> quakeItemWriter) {
    return new StepBuilder("quakeIngestStep", jobRepository)
        .<UsgsFeature, Earthquake>chunk(CHUNK_SIZE, quakeTransactionManager)
        .reader(quakeItemReader)
        .processor(quakeItemProcessor)
        .writer(quakeItemWriter)
        .build();
  }

  /** 지진 수집 Job. */
  @Bean
  public Job quakeIngestJob(JobRepository jobRepository, Step quakeIngestStep) {
    return new JobBuilder("quakeIngestJob", jobRepository).start(quakeIngestStep).build();
  }
}
