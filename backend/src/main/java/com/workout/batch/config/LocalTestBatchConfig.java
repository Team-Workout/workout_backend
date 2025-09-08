package com.workout.batch.config;

import com.workout.member.domain.Member;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
//@Profile({"local", "dev"}) // ★ 중요: 이 설정 파일은 local 또는 dev 환경에서만 활성화됩니다. (prod 제외)
public class LocalTestBatchConfig {

  // 운영용 배치 설정과 동일한 핵심 의존성(JobRepository, TransactionManager, EntityManagerFactory)을 주입받습니다.
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final EntityManagerFactory entityManagerFactory;

  // (JPA 쓰기를 할 때는 EntityManagerFactory가 필수입니다.)

  private static final int CHUNK_SIZE = 100; // DB Write 작업은 API 호출보다 무거울 수 있으므로 청크 크기를 작게 설정

  /**
   * [테스트 Job] 회원 이름 변경 테스트 Job
   */
  @Bean
  public Job testUpdateMemberNameJob() {
    return new JobBuilder("testUpdateMemberNameJob", jobRepository)
        .start(updateMemberNameStep()) // 이 Job은 하나의 Step으로 구성됩니다.
        .build();
  }

  /**
   * [테스트 Step] 회원 Read -> Process (이름 변경) -> Write (DB 저장)
   */
  @Bean
  public Step updateMemberNameStep() {
    return new StepBuilder("updateMemberNameStep", jobRepository)
        // 1. <Input, Output> 타입 모두 Member 입니다.
        .<Member, Member>chunk(CHUNK_SIZE, transactionManager)
        .reader(allMemberReader())        // 2. Reader: 모든 회원을 읽음
        .processor(updateMemberNameProcessor()) // 3. Processor: 회원 이름을 변경
        .writer(memberItemWriter())         // 4. Writer: 변경된 Member 엔티티를 DB에 저장(merge)
        .build();
  }

  /**
   * Reader (읽기): 모든 Member 엔티티를 페이징으로 읽어옵니다.
   */
  @Bean
  @StepScope // JobParameter를 사용할 수 있으나, 이 테스트 Job은 파라미터가 필요 없습니다.
  public JpaPagingItemReader<Member> allMemberReader() {
    log.info("모든 회원 정보 읽기(Reader) 시작");

    // 참고: Member 테이블에 accountStatus가 있으므로
    // 실제로는 "SELECT m FROM Member m WHERE m.accountStatus = 'ACTIVE'" 처럼
    // 활성 회원만 필터링하는 것이 좋지만, 테스트 목적상 모든 회원을 조회합니다.
    return new JpaPagingItemReaderBuilder<Member>()
        .name("allMemberReader")
        .entityManagerFactory(entityManagerFactory)
        .pageSize(CHUNK_SIZE)
        .queryString("SELECT m FROM Member m ORDER BY m.id ASC") //
        .build();
  }

  /**
   * Processor (처리): 회원 이름을 변경합니다.
   */
  @Bean
  @StepScope
  public ItemProcessor<Member, Member> updateMemberNameProcessor() {
    return member -> {
      String originalName = member.getName();
      String newName = originalName + "_BATCH_TEST";

      log.info("회원 이름 변경: (ID: {}) {} -> {}", member.getId(), originalName, newName);

      // 2. setter를 이용해 엔티티의 이름을 변경합니다.
      member.setName(newName);

      // 3. 변경된 엔티티를 Writer로 넘깁니다.
      return member;
    };
  }

  /**
   * Writer (쓰기): 변경된 Member 엔티티 리스트를 DB에 저장합니다.
   * JpaItemWriter는 Processor에서 넘어온 엔티티 리스트를 받아
   * JPA의 'merge' 기능을 사용해 트랜잭션 내에서 일괄 UPDATE/COMMIT 합니다.
   */
  @Bean
  public JpaItemWriter<Member> memberItemWriter() {
    return new JpaItemWriterBuilder<Member>()
        .entityManagerFactory(entityManagerFactory)
        .build();
  }
}