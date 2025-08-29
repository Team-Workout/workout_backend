-- Flyway V2: 초기 마스터 데이터 및 샘플 데이터 삽입 (DML)

-- ########## 1. 헬스장 마스터 데이터 (gym) ##########
INSERT INTO gym (id, name, address, phone_number)
VALUES (1, '파워 피트니스', '부산광역시 해운대구', '051-123-4567'),
       (2, '건강 제일 짐', '부산광역시 서면', '051-987-6543')
ON DUPLICATE KEY UPDATE name         = VALUES(name),
                        address      = VALUES(address),
                        phone_number = VALUES(phone_number);


-- ########## 2. 타겟 근육 마스터 데이터 (muscle) ##########
INSERT INTO muscle (id, name, korean_name, muscle_group)
VALUES (1, 'CHEST', '가슴', 'CHEST'), (2, 'BACK', '등', 'BACK'), (3, 'SHOULDERS', '어깨', 'SHOULDER'),
       (4, 'BICEPS', '이두', 'ARMS'), (5, 'TRICEPS', '삼두', 'ARMS'), (6, 'FOREARM', '전완근', 'ARMS'),
       (7, 'ABS', '복근', 'ABS'), (8, 'GLUTES', '둔근', 'LEGS'), (9, 'QUADS', '대퇴사두', 'LEGS'),
       (10, 'HAMSTRINGS', '햄스트링', 'LEGS'), (11, 'CALVES', '종아리', 'LEGS')
ON DUPLICATE KEY UPDATE name = VALUES(name), korean_name = VALUES(korean_name), muscle_group = VALUES(muscle_group);


-- ########## 3. 운동 마스터 데이터 (exercise) ##########
INSERT INTO exercise (id, name)
VALUES (1, '벤치프레스'), (2, '덤벨 프레스'), (3, '인클라인 벤치프레스'), (4, '딥스'), (5, '푸시업'),
       (6, '풀업'), (7, '랫풀다운'), (8, '바벨 로우'), (9, '데드리프트'), (10, '오버헤드 프레스'),
       (11, '사이드 래터럴 레이즈'), (12, '벤트 오버 래터럴 레이즈'), (13, '스쿼트'), (14, '레그 프레스'),
       (15, '런지'), (16, '바벨 컬'), (17, '덤벨 컬'), (18, '케이블 푸시다운'), (19, '라잉 트라이셉스 익스텐션'),
       (20, '크런치'), (21, '레그 레이즈')
ON DUPLICATE KEY UPDATE name = VALUES(name);


-- ########## 4. 운동-타겟 근육 관계 데이터 (exercise_target_muscle) ##########
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role)
VALUES (1, 1, 'MAIN'), (1, 3, 'SECONDARY'), (1, 5, 'SECONDARY'),
       (2, 1, 'MAIN'), (2, 3, 'SECONDARY'), (2, 5, 'SECONDARY'),
       (3, 1, 'MAIN'), (3, 3, 'SECONDARY'), (4, 1, 'MAIN'), (4, 5, 'MAIN'),
       (5, 1, 'MAIN'), (5, 5, 'SECONDARY'), (5, 7, 'SECONDARY'),
       (6, 2, 'MAIN'), (6, 4, 'SECONDARY'), (7, 2, 'MAIN'), (7, 4, 'SECONDARY'),
       (8, 2, 'MAIN'), (8, 4, 'SECONDARY'), (9, 2, 'MAIN'), (9, 8, 'MAIN'), (9, 10, 'MAIN'),
       (10, 3, 'MAIN'), (10, 5, 'SECONDARY'), (11, 3, 'MAIN'), (12, 3, 'MAIN'),
       (13, 9, 'MAIN'), (13, 8, 'MAIN'), (13, 10, 'SECONDARY'),
       (14, 9, 'MAIN'), (14, 8, 'MAIN'), (15, 9, 'MAIN'), (15, 8, 'MAIN'),
       (16, 4, 'MAIN'), (17, 4, 'MAIN'), (18, 5, 'MAIN'), (19, 5, 'MAIN'),
       (20, 7, 'MAIN'), (21, 7, 'MAIN')
ON DUPLICATE KEY UPDATE muscle_role = VALUES(muscle_role);


-- ########## 5. 사용자/트레이너 통합 샘플 데이터 (member) ##########
-- [수정] V1 스키마에 맞게 is_open_workout_record 컬럼 추가
INSERT INTO member (id, gym_id, email, password, name, gender, account_status, role, is_open_workout_record, introduction, phone_number)
VALUES
    (1, 1, 'chulsoo.kim@example.com', '$2a$10$yS.tJ2a.AXsOM2wD19356uYkS/Fl2i4O12s5lR5h7OJGnNn4AAt/S', '김철수', 'MALE', 'ACTIVE', 'MEMBER', true, NULL, NULL),
    (3, 1, 'minsu.park@example.com', '$2a$10$yS.tJ2a.AXsOM2wD19356uYkS/Fl2i4O12s5lR5h7OJGnNn4AAt/S', '박민수', 'MALE', 'ACTIVE', 'ADMIN', false, NULL, NULL),
    (4, 2, 'jisoo.seo@example.com', '$2a$10$yS.tJ2a.AXsOM2wD19356uYkS/Fl2i4O12s5lR5h7OJGnNn4AAt/S', '서지수', 'FEMALE', 'ACTIVE', 'MEMBER', false, NULL, NULL),
    (5, 2, 'jihye.choi@example.com', '$2a$10$yS.tJ2a.AXsOM2wD19356uYkS/Fl2i4O12s5lR5h7OJGnNn4AAt/S', '최지혜', 'FEMALE', 'SUSPENDED', 'MEMBER', false, NULL, NULL),
    (2, 1, 'younghee.lee@example.com', '$2a$10$yS.tJ2a.AXsOM2wD19356uYkS/Fl2i4O12s5lR5h7OJGnNn4AAt/S', '이영희', 'FEMALE', 'ACTIVE', 'TRAINER', true, '10년 경력의 베테랑 트레이너입니다. 함께 건강한 몸을 만들어봐요!', '010-1234-5678'),
    (6, 1, 'seojun.park@example.com', '$2a$10$yS.tJ2a.AXsOM2wD19356uYkS/Fl2i4O12s5lR5h7OJGnNn4AAt/S', '박서준', 'MALE', 'ACTIVE', 'TRAINER', true, '웨이트 트레이닝 전문가 박서준입니다. 여러분의 잠재력을 끌어올려 드립니다.', '010-9876-5432'),
    (7, 2, 'yuri.choi@example.com', '$2a$10$yS.tJ2a.AXsOM2wD19356uYkS/Fl2i4O12s5lR5h7OJGnNn4AAt/S', '최유리', 'FEMALE', 'ACTIVE', 'TRAINER', true, '여성 전문 트레이너 최유리입니다. 아름다운 바디 라인을 만들어 드립니다.', '010-5555-4444')
ON DUPLICATE KEY UPDATE email = VALUES(email), password = VALUES(password), name = VALUES(name), account_status = VALUES(account_status), is_open_workout_record = VALUES(is_open_workout_record), introduction = VALUES(introduction), phone_number = VALUES(phone_number);


-- ########## 6. 프로필 및 체성분 샘플 데이터 ##########
-- 전문 분야(Specialty) 마스터 데이터
INSERT INTO specialty (id, name) VALUES (1, '다이어트'), (2, '재활운동'), (3, '근력증가'), (4, '바디프로필') ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 트레이너 '이영희'(id=2)의 프로필
INSERT INTO education (member_id, school_name, education_name, degree, start_date, end_date) VALUES (2, '부산대학교', '체육교육과', '학사', '2010-03-01', '2014-02-20') ON DUPLICATE KEY UPDATE school_name=VALUES(school_name);
INSERT INTO work_experience (member_id, work_name, work_place, work_position, work_start, work_end) VALUES (2, '파워 피트니스', '파워 피트니스 해운대점', '수석 트레이너', '2018-03-01', NULL) ON DUPLICATE KEY UPDATE work_name=VALUES(work_name);
INSERT INTO award (member_id, award_name, award_date, award_place) VALUES (2, '전국 생활체육 보디빌딩 대회', '2017-10-15', '3위') ON DUPLICATE KEY UPDATE award_name=VALUES(award_name);
INSERT INTO certification (member_id, certification_name, issuing_organization, acquisition_date) VALUES (2, '생활스포츠지도사 2급 (보디빌딩)', '국민체육진흥공단', '2015-12-01') ON DUPLICATE KEY UPDATE certification_name=VALUES(certification_name);
INSERT INTO trainer_specialty (member_id, specialty_id) VALUES (2, 1), (2, 2) ON DUPLICATE KEY UPDATE member_id=VALUES(member_id);

-- 트레이너 '박서준'(id=6)의 프로필
INSERT INTO education (member_id, school_name, education_name, degree) VALUES (6, '한국체육대학교', '사회체육과', '학사') ON DUPLICATE KEY UPDATE school_name=VALUES(school_name);
INSERT INTO work_experience (member_id, work_name, work_place, work_position, work_start, work_end) VALUES (6, '파워 피트니스', '파워 피트니스 해운대점', '선임 트레이너', '2020-01-01', NULL) ON DUPLICATE KEY UPDATE work_name=VALUES(work_name);
INSERT INTO certification (member_id, certification_name, issuing_organization) VALUES (6, 'NSCA-CPT', 'NSCA') ON DUPLICATE KEY UPDATE certification_name=VALUES(certification_name);
INSERT INTO trainer_specialty (member_id, specialty_id) VALUES (6, 3), (6, 4) ON DUPLICATE KEY UPDATE member_id=VALUES(member_id);

-- 사용자 '김철수'(id=1)의 체성분
INSERT INTO body_composition (member_id, measurement_date, weight_kg, fat_kg, muscle_mass_kg)
VALUES (1, '2025-07-15', 85, 20, 38), (1, '2025-08-15', 82, 17, 39)
ON DUPLICATE KEY UPDATE weight_kg=VALUES(weight_kg);


-- ########## 7. PT 관련 샘플 데이터 (신청 -> 계약 -> 예약 -> 세션) ##########
-- 트레이너 '이영희'(id=2)가 PT 상품(오퍼링)을 등록
INSERT INTO pt_offering (id, trainer_id, gym_id, title, description, price, total_sessions, status)
VALUES (1, 2, 1, '12주 바디프로필 완성반', '12주 동안 주 2회씩 진행되는 바디프로필 전문 PT입니다. 식단 관리 포함.', 1500000, 24, 'ACTIVE')
ON DUPLICATE KEY UPDATE title = VALUES(title), price = VALUES(price);

-- 회원 '김철수'(id=1)가 위 PT 상품을 신청하여 바로 '승인'된 상태로 생성
-- [수정] PENDING 후 UPDATE 하는 대신, 바로 APPROVED 상태로 INSERT하여 스크립트 간소화
INSERT INTO pt_application (id, offering_id, member_id, pt_application_status, total_sessions)
VALUES (1, 1, 1, 'APPROVED', 24)
ON DUPLICATE KEY UPDATE pt_application_status = VALUES(pt_application_status);

-- 위에서 '승인'된 신청(id=1)을 기반으로 PT 계약 생성
INSERT INTO pt_contract (id, gym_id, application_id, member_id, trainer_id, status, price, payment_date, start_date, total_sessions, remaining_sessions)
VALUES (1, 1, 1, 1, 2, 'ACTIVE', 1500000, '2025-08-20', '2025-08-22', 24, 24)
ON DUPLICATE KEY UPDATE status = VALUES(status), remaining_sessions = VALUES(remaining_sessions);

-- 생성된 계약(id=1)에 대한 첫 번째 PT 수업 예약
INSERT INTO pt_appointment (id, contract_id, status, start_time, end_time)
VALUES (1, 1, 'SCHEDULED', '2025-08-22 10:00:00', '2025-08-22 11:00:00')
ON DUPLICATE KEY UPDATE status = VALUES(status), start_time = VALUES(start_time);

-- 회원 '김철수'(id=1)의 PT 수업(appointment_id=1)에 대한 운동일지 생성
INSERT INTO workout_log (id, member_id, workout_date) VALUES (1, 1, '2025-08-22') ON DUPLICATE KEY UPDATE workout_date=VALUES(workout_date);
INSERT INTO workout_exercise (id, workout_log_id, exercise_id, log_order) VALUES (1, 1, 1, 1) ON DUPLICATE KEY UPDATE log_order=VALUES(log_order); -- 벤치프레스
INSERT INTO workout_set (id, workout_exercise_id, set_order, weight, reps) VALUES (1, 1, 1, 80.0, 10) ON DUPLICATE KEY UPDATE weight=VALUES(weight);

-- 위에서 생성된 운동일지(id=1)와 PT예약(id=1)을 PT세션으로 연결
INSERT INTO pt_session (id, workout_log_id, appointment_id) VALUES (1, 1, 1) ON DUPLICATE KEY UPDATE workout_log_id=VALUES(workout_log_id);


-- ########## 8. 마스터 데이터 버전 초기화 ##########
INSERT INTO master_data_version (data_type, version, updated_at)
VALUES ('EXERCISE', 1, NOW()),
       ('MUSCLE', 1, NOW()),
       ('EXERCISE_TARGET_MUSCLE', 1, NOW()) -- [추가] 다른 마스터 데이터에 대한 버전도 초기화해주는 것이 좋습니다.
ON DUPLICATE KEY UPDATE version = VALUES(version), updated_at = VALUES(updated_at);