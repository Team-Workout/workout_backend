package com.workout.batch.scheduler;

import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchJobScheduler {

  private final JobLauncher jobLauncher;
  private final Job sendPtReminderJob;


  @Scheduled(cron = "0 0 20 * * *") // 초 분 시 일 월 요일
  public void runPtReminderJob() {
    try {
      // 1. "내일" 날짜 계산
      LocalDate tomorrow = LocalDate.now().plusDays(1);

      JobParameters jobParameters = new JobParametersBuilder()
          .addString("runId", UUID.randomUUID().toString()) // 매번 새로운 JobInstance를 생성하기 위함
          .addString("targetDate", tomorrow.toString()) // "yyyy-MM-dd" 형식
          .toJobParameters();

      log.info("PT 알림 배치 작업을 시작합니다. 대상 날짜: {}", tomorrow);
      jobLauncher.run(sendPtReminderJob, jobParameters);

    } catch (Exception e) {
      log.error("PT 알림 배치 작업 실행 중 오류 발생", e);
    }
  }
}