package com.workout.test; // 실제 파일의 패키지 경로에 맞게 수정하세요

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

@Component
@Profile("test")
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

  private final JdbcTemplate jdbcTemplate;
  private final Random random = new Random();
  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  private static final int GYM_COUNT = 50;
  private static final int TRAINER_COUNT = 250;
  private static final int MEMBER_COUNT = 5000;
  private static final int CONTRACT_COUNT = 2000;
  private static final int APPOINTMENT_COUNT = 50000; // FCM 테스트용
  private static final int FEED_COUNT = 10000;      // 캐싱 테스트용
  private static final int COMMENTS_PER_FEED = 5;
  private static final int LIKES_PER_FEED = 10;
  private static final int CHUNK_SIZE = 2000; // 청크 사이즈 조정
  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    System.out.println("Deleting all existing data for a clean test run...");
    jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
    jdbcTemplate.execute("TRUNCATE TABLE likes");
    jdbcTemplate.execute("TRUNCATE TABLE comment");
    jdbcTemplate.execute("TRUNCATE TABLE feed");
    jdbcTemplate.execute("TRUNCATE TABLE pt_session");
    jdbcTemplate.execute("TRUNCATE TABLE workout_set");
    jdbcTemplate.execute("TRUNCATE TABLE workout_exercise");
    jdbcTemplate.execute("TRUNCATE TABLE workout_log");
    jdbcTemplate.execute("TRUNCATE TABLE pt_appointment");
    jdbcTemplate.execute("TRUNCATE TABLE pt_contract");
    jdbcTemplate.execute("TRUNCATE TABLE pt_application");
    jdbcTemplate.execute("TRUNCATE TABLE pt_offering");
    jdbcTemplate.execute("TRUNCATE TABLE trainer_specialty");
    jdbcTemplate.execute("TRUNCATE TABLE specialty");
    jdbcTemplate.execute("TRUNCATE TABLE member");
    jdbcTemplate.execute("TRUNCATE TABLE gym");
    jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    System.out.println("All tables truncated successfully.");

    System.out.println("Starting valid large-scale test data generation...");
    long startTime = System.currentTimeMillis();

    // STEP 1: Gym 생성
    List<Object[]> gyms = IntStream.range(0, GYM_COUNT)
        .mapToObj(i -> new Object[]{"헬스장 " + (i + 1), "주소 " + (i + 1), "010-0000-" + String.format("%04d", i)})
        .collect(Collectors.toList());
    jdbcTemplate.batchUpdate("INSERT INTO gym (name, address, phone_number) VALUES (?, ?, ?)", gyms);
    System.out.println(GYM_COUNT + " gyms created.");

    // STEP 2: Member 및 Trainer 생성
    String encodedPassword = passwordEncoder.encode("password123");
    String[] genders = {"MALE", "FEMALE"};
    List<Long> memberIds = new ArrayList<>();
    List<Long> trainerIds = new ArrayList<>();
    AtomicLong currentMemberId = new AtomicLong(1);

    List<Object[]> trainers = new ArrayList<>();
    for (int i = 0; i < TRAINER_COUNT; i++) {
      trainers.add(createMemberParams(encodedPassword, genders, "TRAINER", i));
      trainerIds.add(currentMemberId.getAndIncrement());
    }
    jdbcTemplate.batchUpdate("INSERT INTO member (gym_id, name, email, password, gender, account_status, role, fcm_token, profile_image_uri) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", trainers);
    System.out.println(TRAINER_COUNT + " trainers created.");

    List<Object[]> members = new ArrayList<>();
    for (int i = 0; i < MEMBER_COUNT; i++) {
      members.add(createMemberParams(encodedPassword, genders, "MEMBER", i));
      memberIds.add(currentMemberId.getAndIncrement());
    }
    jdbcTemplate.batchUpdate("INSERT INTO member (gym_id, name, email, password, gender, account_status, role, fcm_token, profile_image_uri) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", members);
    System.out.println(MEMBER_COUNT + " members created.");

    // STEP 3: PT 관련 데이터 (Offering -> Application -> Contract)
    long trainerIdForOffering = trainerIds.get(0);
    long gymIdForOffering = 1L;
    jdbcTemplate.update("INSERT INTO pt_offering (id, trainer_id, gym_id, title, price, total_sessions, status) VALUES (1, ?, ?, 'Temp Offering', 1000, 10, 'ACTIVE')",
        trainerIdForOffering, gymIdForOffering);

    long memberIdForApplication = memberIds.get(0);
    jdbcTemplate.update("INSERT INTO pt_application (id, offering_id, member_id, pt_application_status) VALUES (1, 1, ?, 'APPROVED')", memberIdForApplication);

    List<Object[]> contracts = new ArrayList<>();
    for (int i = 0; i < CONTRACT_COUNT; i++) {
      contracts.add(new Object[]{(long) (random.nextInt(GYM_COUNT) + 1), 1L, memberIds.get(random.nextInt(MEMBER_COUNT)), trainerIds.get(random.nextInt(TRAINER_COUNT)), "ACTIVE", 24, 24});
    }
    jdbcTemplate.batchUpdate("INSERT INTO pt_contract (gym_id, application_id, member_id, trainer_id, status, total_sessions, remaining_sessions) VALUES (?, ?, ?, ?, ?, ?, ?)", contracts);
    System.out.println(CONTRACT_COUNT + " PT contracts created.");
    List<Long> contractIds = LongStream.rangeClosed(1, CONTRACT_COUNT).boxed().collect(Collectors.toList());

    // [수정] STEP 4: PT Appointment 생성 (분(minute)까지 랜덤화하여 중복 회피)
    System.out.println("Generating " + APPOINTMENT_COUNT + " appointments in chunks...");
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    for (int i = 0; i < APPOINTMENT_COUNT; i += CHUNK_SIZE) {
      List<Object[]> appointmentsChunk = new ArrayList<>();
      int end = Math.min(i + CHUNK_SIZE, APPOINTMENT_COUNT);
      for (int j = i; j < end; j++) {
        // 시간(9~20시)과 분(0~59분)을 모두 랜덤으로 생성하여 중복 가능성을 크게 낮춤
        LocalDateTime appointmentStartTime = tomorrow.atTime(random.nextInt(12) + 9, random.nextInt(60));
        appointmentsChunk.add(new Object[]{contractIds.get(random.nextInt(CONTRACT_COUNT)), "SCHEDULED", appointmentStartTime, appointmentStartTime.plusHours(1)});
      }
      // ON DUPLICATE KEY UPDATE 구문으로 만에 하나 발생할 수 있는 중복 에러를 무시하고 넘어감
      jdbcTemplate.batchUpdate("INSERT INTO pt_appointment (contract_id, status, start_time, end_time) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE status=status", appointmentsChunk);
      if((i + CHUNK_SIZE) % (CHUNK_SIZE * 10) == 0) System.out.println("  " + end + " / " + APPOINTMENT_COUNT + " appointments created...");
    }
    System.out.println(APPOINTMENT_COUNT + " PT appointments created successfully.");

    // STEP 5: Feed, Comment, Likes 생성 (캐싱 테스트용)
    generateFeedRelatedData(memberIds, trainerIds);

    long endTime = System.currentTimeMillis();
    System.out.println("Test data generation completed successfully in " + (endTime - startTime) / 1000 + " seconds.");
  }

  private Object[] createMemberParams(String encodedPassword, String[] genders, String role, int index) {
    String email = role.toLowerCase() + "_" + index + "@test.com";
    String name = role.equals("TRAINER") ? "트레이너 " + index : "회원 " + index;
    String fcmToken = (random.nextInt(10) == 0) ? null : "fake-fcm-token-" + UUID.randomUUID().toString();
    String profileImageUri = "default-profile.png";

    return new Object[]{
        (long) (random.nextInt(GYM_COUNT) + 1),
        name,
        email,
        encodedPassword,
        genders[random.nextInt(2)],
        "ACTIVE",
        role,
        fcmToken,
        profileImageUri
    };
  }

  private void generateFeedRelatedData(List<Long> memberIds, List<Long> trainerIds) {
    List<Long> allUserIds = new ArrayList<>(memberIds);
    allUserIds.addAll(trainerIds);
    Collections.shuffle(allUserIds);

    System.out.println("Generating " + FEED_COUNT + " feeds in chunks...");
    AtomicLong currentFeedId = new AtomicLong(1);
    List<Long> feedIds = new ArrayList<>();

    for (int i = 0; i < FEED_COUNT; i += CHUNK_SIZE) {
      List<Object[]> feedsChunk = new ArrayList<>();
      int end = Math.min(i + CHUNK_SIZE, FEED_COUNT);
      for (int j = i; j < end; j++) {
        feedsChunk.add(new Object[]{
            (long) (random.nextInt(GYM_COUNT) + 1),
            allUserIds.get(random.nextInt(allUserIds.size())),
            "https://example.com/image-" + j + ".jpg"
        });
        feedIds.add(currentFeedId.getAndIncrement());
      }
      jdbcTemplate.batchUpdate("INSERT INTO feed (gym_id, member_id, image_url) VALUES (?, ?, ?)", feedsChunk);
      if((i + CHUNK_SIZE) % (CHUNK_SIZE * 10) == 0) System.out.println("  " + end + " / " + FEED_COUNT + " feeds created...");
    }
    System.out.println(FEED_COUNT + " feeds created successfully.");

    long totalComments = (long) FEED_COUNT * COMMENTS_PER_FEED;
    System.out.println("Generating " + totalComments + " comments in chunks...");
    for (int i = 0; i < totalComments; i += CHUNK_SIZE) {
      List<Object[]> commentsChunk = new ArrayList<>();
      int end = (int) Math.min(i + CHUNK_SIZE, totalComments);
      for (int j = i; j < end; j++) {
        commentsChunk.add(new Object[]{
            feedIds.get(random.nextInt(feedIds.size())),
            allUserIds.get(random.nextInt(allUserIds.size())),
            "테스트 댓글 내용 " + j
        });
      }
      jdbcTemplate.batchUpdate("INSERT INTO comment (feed_id, member_id, content) VALUES (?, ?, ?)", commentsChunk);
      if((i + CHUNK_SIZE) % (CHUNK_SIZE * 10) == 0) System.out.println("  " + end + " / " + totalComments + " comments created...");
    }
    System.out.println(totalComments + " comments created successfully.");

    long totalLikes = (long) FEED_COUNT * LIKES_PER_FEED;
    System.out.println("Generating " + totalLikes + " likes in chunks...");
    for (int i = 0; i < totalLikes; i += CHUNK_SIZE) {
      List<Object[]> likesChunk = new ArrayList<>();
      int end = (int) Math.min(i + CHUNK_SIZE, totalLikes);
      for (int j = i; j < end; j++) {
        likesChunk.add(new Object[]{
            allUserIds.get(j % allUserIds.size()),
            "FEED",
            feedIds.get(random.nextInt(feedIds.size()))
        });
      }
      jdbcTemplate.batchUpdate("INSERT INTO likes (member_id, target_type, target_id) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE member_id = member_id", likesChunk);
      if((i + CHUNK_SIZE) % (CHUNK_SIZE * 10) == 0) System.out.println("  " + end + " / " + totalLikes + " likes created...");
    }
    System.out.println(totalLikes + " likes created successfully.");
  }
}