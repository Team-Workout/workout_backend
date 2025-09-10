package com.workout.batch.config;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.workout.member.domain.Member;
import com.workout.notification.dto.FcmRequest;
import com.workout.notification.service.FcmService;
import com.workout.pt.domain.contract.PTAppointment;
import com.workout.pt.domain.contract.PTAppointmentStatus;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

@Slf4j
@Configuration
public class SendPtReminderBatchConfig {

  private static final int CHUNK_SIZE = 500;
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final EntityManagerFactory entityManagerFactory;
  private final FcmService fcmService;

  public SendPtReminderBatchConfig(JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      EntityManagerFactory entityManagerFactory, FcmService fcmService) {
    this.entityManagerFactory = entityManagerFactory;
    this.jobRepository = jobRepository;
    this.transactionManager = transactionManager;
    this.fcmService = fcmService;
  }

  @Bean
  public Job sendPtReminderJob() {
    return new JobBuilder("sendPtReminderJob", jobRepository)
        .start(sendPtReminderStep())
        .build();
  }

  @Bean
  public Step sendPtReminderStep() {
    return new StepBuilder("sendPtReminderStep", jobRepository)
        .<PTAppointment, FcmRequest>chunk(CHUNK_SIZE, transactionManager)
        .reader(ptAppointmentReader(null))
        .processor(ptAppointmentToMessageProcessor())
        .writer(fcmMessageWriter())
        .faultTolerant() // 장애 극복 기능 활성화
        .skip(NullPointerException.class) // Processor에서 NPE가 발생하면
        .skipLimit(100) // Job 실행 당 최대 100개까지만 "스킵(무시)"하고 계속 진행
        .build();
  }

  @Bean
  @StepScope
  public JpaPagingItemReader<PTAppointment> ptAppointmentReader(
      @Value("#{jobParameters['targetDate']}") String targetDateStr) {

    LocalDate targetDate = LocalDate.parse(targetDateStr);
    LocalDateTime startOfDay = targetDate.atStartOfDay();
    LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);

    log.info("알림 대상 PT 약속 조회 시작. 범위: {} ~ {}", startOfDay, endOfDay);

    return new JpaPagingItemReaderBuilder<PTAppointment>()
        .name("ptAppointmentReader")
        .entityManagerFactory(entityManagerFactory)
        .pageSize(CHUNK_SIZE)
        .queryString(
            "SELECT p FROM PTAppointment p " +
                "JOIN FETCH p.contract c " +
                "JOIN FETCH c.member m " +
                "WHERE p.startTime BETWEEN :startOfDay AND :endOfDay " +
                "AND p.status = :status") // <- 파라미터로 변경
        .parameterValues(Map.of(
            "startOfDay", startOfDay,
            "endOfDay", endOfDay,
            "status", PTAppointmentStatus.SCHEDULED // <- Enum 객체를 직접 전달
        ))
        .build();
  }

  @Bean
  @StepScope
  public ItemProcessor<PTAppointment, FcmRequest> ptAppointmentToMessageProcessor() {
    return ptAppointment -> {
      Member member = ptAppointment.getContract().getMember();

      if (member == null || !StringUtils.hasText(member.getFcmToken())) {
        log.warn("PTAppointment ID {}에 연결된 멤버 또는 FCM 토큰이 없습니다. 발송 skip.", ptAppointment.getId());
        return null;
      }

      String fcmToken = member.getFcmToken();
      String title = "🏋️ PT 예약 알림";
      String body = String.format("%s님, 내일 (%s) PT 예약이 있습니다. 잊지 마세요!",
          member.getName(),
          ptAppointment.getStartTime().toLocalTime().toString()
      );

      Notification notification = Notification.builder()
          .setTitle(title)
          .setBody(body)
          .build();

      Message messagePayload = Message.builder()
          .setToken(fcmToken)
          .setNotification(notification)
          .build();

      return new FcmRequest(fcmToken, messagePayload);
    };
  }

  @Bean
  @StepScope
  public ItemWriter<FcmRequest> fcmMessageWriter() {
    return chunk -> {
      log.info("FCM API 호출. {}건 일괄 발송 시도.", chunk.getItems().size());
      List<FcmRequest> requestList = new java.util.ArrayList<>(chunk.getItems());
      fcmService.sendAllNotifications(requestList);
    };
  }
}