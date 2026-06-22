package com.asdf.minilog.batch.quake;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 지진 수집 Job을 주기적으로 실행하는 스케줄러.
 *
 * <p>매 실행 시 [현재−15분, 현재] 구간을 JobParameter로 전달한다. 구간을 약간 겹치게 잡고 업서트로 중복을 흡수하여 이벤트 누락을 방지한다. 시작/종료
 * 시각이 매번 달라 JobInstance가 매 실행 새로 생성된다.
 */
@Component
public class QuakeBatchScheduler {

  private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  private final JobLauncher jobLauncher;
  private final Job quakeIngestJob;

  public QuakeBatchScheduler(JobLauncher jobLauncher, Job quakeIngestJob) {
    this.jobLauncher = jobLauncher;
    this.quakeIngestJob = quakeIngestJob;
  }

  /** 설정된 주기(기본 10분)마다 지진 수집 Job을 실행한다. */
  @Scheduled(fixedDelayString = "${quake.batch.interval-ms:600000}")
  public void runQuakeIngestJob() throws Exception {
    LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
    JobParameters params =
        new JobParametersBuilder()
            .addString("startTime", now.minusMinutes(15).format(ISO))
            .addString("endTime", now.format(ISO))
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();
    jobLauncher.run(quakeIngestJob, params);
  }
}
