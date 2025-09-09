package com.workout.batch.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/test/batch")
public class BatchTriggerController {

  private final JobLauncher jobLauncher;

  // 2. [기존] 운영용 알림 Job 주입
  private final Job sendPtReminderJob;

  // 3. [신규] 방금 만든 테스트용 Job을 이름으로(@Qualifier) 주입받습니다.
  private final Job testUpdateMemberNameJob;


  public BatchTriggerController(JobLauncher jobLauncher,
      @Qualifier("sendPtReminderJob") Job sendPtReminderJob,
      @Qualifier("testUpdateMemberNameJob") Job testUpdateMemberNameJob) {
    this.jobLauncher = jobLauncher;
    this.sendPtReminderJob = sendPtReminderJob;
    this.testUpdateMemberNameJob = testUpdateMemberNameJob;
  }

  // 5. [기존] PT 알림 배치 수동 실행 API (그대로 둠)
  @PostMapping("/pt-reminder")
  public ResponseEntity<String> runPtReminderJobManually(
      @RequestParam(required = false) String date) {

    LocalDate targetDate;
    try {
      // 파라미터로 날짜가 들어오면 그 날짜를 targetDate로 사용
      targetDate = (date != null) ? LocalDate.parse(date) : LocalDate.now().plusDays(1);
    } catch (DateTimeParseException e) {
      return ResponseEntity.badRequest().body("잘못된 날짜 형식입니다. (YYYY-MM-DD)");
    }

    try {
      // 스케줄러가 하는 일과 완벽히 동일한 JobParameters를 생성합니다.
      // (UUID로 매번 고유 ID를 줘야 재실행이 가능합니다)
      JobParameters jobParameters = new JobParametersBuilder()
          .addString("runId", UUID.randomUUID().toString()) // 매번 새로운 JobInstance를 생성
          .addString("targetDate", targetDate.toString()) // "yyyy-MM-dd" 형식
          .toJobParameters();

      log.info("수동 배치 작업을 시작합니다. 대상 날짜: {}", targetDate);

      // Job 실행
      jobLauncher.run(sendPtReminderJob, jobParameters);

      return ResponseEntity.ok(targetDate + " 대상 배치 작업 시작됨.");

    } catch (Exception e) {
      log.error("수동 배치 작업 실행 중 오류 발생", e);
      return ResponseEntity.internalServerError().body("배치 실행 실패: " + e.getMessage());
    }
  }

  /**
   * 6. [신규] 회원 이름 변경 테스트 배치를 수동으로 실행하는 API
   */
  @PostMapping("/update-member-names")
  public ResponseEntity<String> runUpdateMemberNamesJob() {
    try {
      // 이 Job은 별도 파라미터가 필요 없지만, JobInstance를 매번 새로 생성하기 위해
      // 고유 ID (여기서는 현재 시간)를 파라미터로 넘겨야 합니다.
      JobParameters jobParameters = new JobParametersBuilder()
          .addLong("runAt", System.currentTimeMillis())
          .toJobParameters();

      log.info("수동 테스트 배치 (회원 이름 변경) 작업을 시작합니다.");

      // "testUpdateMemberNameJob" Bean을 실행합니다.
      jobLauncher.run(testUpdateMemberNameJob, jobParameters);

      return ResponseEntity.ok("회원 이름 변경 배치 작업 시작됨.");

    } catch (Exception e) {
      log.error("수동 배치 작업 (회원 이름 변경) 실행 중 오류 발생", e);
      return ResponseEntity.internalServerError().body("배치 실행 실패: " + e.getMessage());
    }
  }
}