-- Flyway V2: 초기 마스터 데이터 및 샘플 데이터 삽입 (DML) (수정된 버전)
-- [수정 1] 모든 ON DUPLICATE KEY UPDATE 구문에서 deprecated된 VALUES() 대신 AS alias 구문 사용
-- [수정 2] MuscleRole Enum(PRIMARY, SECONDARY) 정의에 맞게 'MAIN' -> 'PRIMARY'로 변경

-- ########## 1. 헬스장 마스터 데이터 (gym) ##########
INSERT INTO gym (id, name, address, phone_number)
    VALUES (1, '파워 피트니스', '부산광역시 해운대구', '051-123-4567'),
           (2, '건강 제일 짐', '부산광역시 서면', '051-987-6543')
        AS newData
ON DUPLICATE KEY UPDATE name         = newData.name,
                        address      = newData.address,
                        phone_number = newData.phone_number;


-- ########## 2. 타겟 근육 마스터 데이터 (muscle) ##########
INSERT INTO muscle (id, name, korean_name, muscle_group)
    VALUES (1, 'CHEST', '가슴', 'CHEST'),
           (2, 'BACK', '등', 'BACK'),
           (3, 'SHOULDERS', '어깨', 'SHOULDER'),
           (4, 'BICEPS', '이두', 'ARMS'),
           (5, 'TRICEPS', '삼두', 'ARMS'),
           (6, 'FOREARM', '전완근', 'ARMS'),
           (7, 'ABS', '복근', 'ABS'),
           (8, 'GLUTES', '둔근', 'LEGS'),
           (9, 'QUADS', '대퇴사두', 'LEGS'),
           (10, 'HAMSTRINGS', '햄스트링', 'LEGS'),
           (11, 'CALVES', '종아리', 'LEGS')
        AS newData
ON DUPLICATE KEY UPDATE name         = newData.name,
                        korean_name  = newData.korean_name,
                        muscle_group = newData.muscle_group;


-- ########## 3. 운동 마스터 데이터 (exercise) ##########
INSERT INTO exercise (id, name)
    VALUES (1, '벤치프레스'),
           (2, '덤벨 프레스'),
           (3, '인클라인 벤치프레스'),
           (4, '딥스'),
           (5, '푸시업'),
           (6, '풀업'),
           (7, '랫풀다운'),
           (8, '바벨 로우'),
           (9, '데드리프트'),
           (10, '오버헤드 프레스'),
           (11, '사이드 래터럴 레이즈'),
           (12, '벤트 오버 래터럴 레이즈'),
           (13, '스쿼트'),
           (14, '레그 프레스'),
           (15, '런지'),
           (16, '바벨 컬'),
           (17, '덤벨 컬'),
           (18, '케이블 푸시다운'),
           (19, '라잉 트라이셉스 익스텐션'),
           (20, '크런치'),
           (21, '레그 레이즈')
        AS newData
ON DUPLICATE KEY UPDATE name = newData.name;


-- ########## 4. 운동-타겟 근육 관계 데이터 (exercise_target_muscle) ##########
-- [수정] MuscleRole.java Enum에 따라 'MAIN'을 'PRIMARY'로 모두 변경
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role)
    VALUES (1, 1, 'PRIMARY'),
           (1, 3, 'SECONDARY'),
           (1, 5, 'SECONDARY'),
           (2, 1, 'PRIMARY'),
           (2, 3, 'SECONDARY'),
           (2, 5, 'SECONDARY'),
           (3, 1, 'PRIMARY'),
           (3, 3, 'SECONDARY'),
           (4, 1, 'PRIMARY'),
           (4, 5, 'PRIMARY'),
           (5, 1, 'PRIMARY'),
           (5, 5, 'SECONDARY'),
           (5, 7, 'SECONDARY'),
           (6, 2, 'PRIMARY'),
           (6, 4, 'SECONDARY'),
           (7, 2, 'PRIMARY'),
           (7, 4, 'SECONDARY'),
           (8, 2, 'PRIMARY'),
           (8, 4, 'SECONDARY'),
           (9, 2, 'PRIMARY'),
           (9, 8, 'PRIMARY'),
           (9, 10, 'PRIMARY'),
           (10, 3, 'PRIMARY'),
           (10, 5, 'SECONDARY'),
           (11, 3, 'PRIMARY'),
           (12, 3, 'PRIMARY'),
           (13, 9, 'PRIMARY'),
           (13, 8, 'PRIMARY'),
           (13, 10, 'SECONDARY'),
           (14, 9, 'PRIMARY'),
           (14, 8, 'PRIMARY'),
           (15, 9, 'PRIMARY'),
           (15, 8, 'PRIMARY'),
           (16, 4, 'PRIMARY'),
           (17, 4, 'PRIMARY'),
           (18, 5, 'PRIMARY'),
           (19, 5, 'PRIMARY'),
           (20, 7, 'PRIMARY'),
           (21, 7, 'PRIMARY')
        AS newData
ON DUPLICATE KEY UPDATE muscle_role = newData.muscle_role;


-- ########## 5. 사용자/트레이너 통합 샘플 데이터 (member) ##########
INSERT INTO member (id, gym_id, email, password, name, gender, account_status, role,
                    is_open_workout_record, profile_image_uri, introduction, phone_number)
    VALUES (1, 1, 'chulsoo.kim@example.com',
            '$2a$10$M2dz9c9du4niNsMGQFqGZ.tFM3YIGhLqjAnZbzo7Yx7/xqVPZu26i', '김철수', 'MALE', 'ACTIVE',
            'MEMBER', true, 'default-profile.png', NULL, NULL),
           (3, 1, 'minsu.park@example.com',
            '$2a$10$M2dz9c9du4niNsMGQFqGZ.tFM3YIGhLqjAnZbzo7Yx7/xqVPZu26i', '박민수', 'MALE', 'ACTIVE',
            'ADMIN', false, 'default-profile.png', NULL, NULL),
           (4, 2, 'jisoo.seo@example.com',
            '$2a$10$M2dz9c9du4niNsMGQFqGZ.tFM3YIGhLqjAnZbzo7Yx7/xqVPZu26i', '서지수', 'FEMALE',
            'ACTIVE', 'MEMBER', false, 'default-profile.png', NULL, NULL),
           (5, 2, 'jihye.choi@example.com',
            '$2a$10$M2dz9c9du4niNsMGQFqGZ.tFM3YIGhLqjAnZbzo7Yx7/xqVPZu26i', '최지혜', 'FEMALE',
            'SUSPENDED', 'MEMBER', false, 'default-profile.png', NULL, NULL),
           (2, 1, 'younghee.lee@example.com',
            '$2a$10$yS.tJ2a.AXsOM2wD19356uYkS/Fl2i4O12s5lR5h7OJGnNn4AAt/S', '이영희', 'FEMALE',
            'ACTIVE', 'TRAINER', true, 'trainer-yh-lee.png',
            '10년 경력의 베테랑 트레이너입니다. 함께 건강한 몸을 만들어봐요!', '010-1234-5678'),
           (6, 1, 'seojun.park@example.com',
            '$2a$10$yS.tJ2a.AXsOM2wD19356uYkS/Fl2i4O12s5lR5h7OJGnNn4AAt/S', '박서준', 'MALE', 'ACTIVE',
            'TRAINER', true, 'trainer-sj-park.png', '웨이트 트레이닝 전문가 박서준입니다. 여러분의 잠재력을 끌어올려 드립니다.',
            '010-9876-5432'),
           (7, 2, 'yuri.choi@example.com',
            '$2a$10$yS.tJ2a.AXsOM2wD19356uYkS/Fl2i4O12s5lR5h7OJGnNn4AAt/S', '최유리', 'FEMALE',
            'ACTIVE', 'TRAINER', true, 'trainer-yr-choi.png',
            '여성 전문 트레이너 최유리입니다. 아름다운 바디 라인을 만들어 드립니다.', '010-5555-4444')
        AS newData
ON DUPLICATE KEY UPDATE email                  = newData.email,
                        password               = newData.password,
                        name                   = newData.name,
                        account_status         = newData.account_status,
                        is_open_workout_record = newData.is_open_workout_record,
                        profile_image_uri      = newData.profile_image_uri,
                        introduction           = newData.introduction,
                        phone_number           = newData.phone_number;


-- ########## 6. 프로필 및 체성분 샘플 데이터 ##########
-- 전문 분야(Specialty) 마스터 데이터
INSERT INTO specialty (id, name) VALUES (1, '다이어트'),
                                        (2, '재활운동'),
                                        (3, '근력증가'),
                                        (4, '바디프로필') AS newData
ON DUPLICATE KEY UPDATE name = newData.name;

-- 트레이너 '이영희'(id=2)의 프로필
INSERT INTO education (member_id, school_name, education_name, degree, start_date,
                       end_date) VALUES (2, '부산대학교', '체육교육과', '학사', '2010-03-01',
                                         '2014-02-20') AS newData
ON DUPLICATE KEY UPDATE school_name=newData.school_name;
INSERT INTO work_experience (member_id, work_name, work_place, work_position, work_start,
                             work_end) VALUES (2, '파워 피트니스', '파워 피트니스 해운대점', '수석 트레이너',
                                               '2018-03-01', NULL) AS newData
ON DUPLICATE KEY UPDATE work_name=newData.work_name;
INSERT INTO award (member_id, award_name, award_date, award_place) VALUES (2, '전국 생활체육 보디빌딩 대회', '2017-10-15', '3위') AS newData
ON DUPLICATE KEY UPDATE award_name=newData.award_name;
INSERT INTO certification (member_id, certification_name, issuing_organization,
                           acquisition_date) VALUES (2, '생활스포츠지도사 2급 (보디빌딩)', '국민체육진흥공단', '2015-12-01') AS newData
ON DUPLICATE KEY UPDATE certification_name=newData.certification_name;
INSERT INTO trainer_specialty (member_id, specialty_id) VALUES (2, 1), (2, 2) AS newData
ON DUPLICATE KEY UPDATE member_id=newData.member_id;

-- 트레이너 '박서준'(id=6)의 프로필
INSERT INTO education (member_id, school_name, education_name, degree) VALUES (6, '한국체육대학교', '사회체육과', '학사') AS newData
ON DUPLICATE KEY UPDATE school_name=newData.school_name;
INSERT INTO work_experience (member_id, work_name, work_place, work_position, work_start,
                             work_end) VALUES (6, '파워 피트니스', '파워 피트니스 해운대점', '선임 트레이너',
                                               '2020-01-01', NULL) AS newData
ON DUPLICATE KEY UPDATE work_name=newData.work_name;
INSERT INTO certification (member_id, certification_name, issuing_organization) VALUES (6, 'NSCA-CPT', 'NSCA') AS newData
ON DUPLICATE KEY UPDATE certification_name=newData.certification_name;
INSERT INTO trainer_specialty (member_id, specialty_id) VALUES (6, 3), (6, 4) AS newData
ON DUPLICATE KEY UPDATE member_id=newData.member_id;

-- 사용자 '김철수'(id=1)의 체성분
INSERT INTO body_composition (member_id, measurement_date, weight_kg, fat_kg, muscle_mass_kg)
    VALUES (1, '2025-07-15', 85, 20, 38), (1, '2025-08-15', 82, 17, 39)
        AS newData
ON DUPLICATE KEY UPDATE weight_kg=newData.weight_kg;


-- ########## 7. PT 관련 샘플 데이터 (신청 -> 계약 -> 예약 -> 세션) ##########
-- 트레이너 '이영희'(id=2)가 PT 상품(오퍼링)을 등록
INSERT INTO pt_offering (id, trainer_id, gym_id, title, description, price, total_sessions, status)
    VALUES (1, 2, 1, '12주 바디프로필 완성반', '12주 동안 주 2회씩 진행되는 바디프로필 전문 PT입니다. 식단 관리 포함.', 1500000, 24,
            'ACTIVE')
        AS newData
ON DUPLICATE KEY UPDATE title = newData.title,
                        price = newData.price;

-- 회원 '김철수'(id=1)가 위 PT 상품을 신청하여 바로 '승인'된 상태로 생성
INSERT INTO pt_application (id, offering_id, member_id, pt_application_status, total_sessions)
    VALUES (1, 1, 1, 'APPROVED', 24)
        AS newData
ON DUPLICATE KEY UPDATE pt_application_status = newData.pt_application_status;

-- 위에서 '승인'된 신청(id=1)을 기반으로 PT 계약 생성
INSERT INTO pt_contract (id, gym_id, application_id, member_id, trainer_id, status, price,
                         payment_date, start_date, total_sessions, remaining_sessions)
    VALUES (1, 1, 1, 1, 2, 'ACTIVE', 1500000, '2025-08-20', '2025-08-22', 24, 24)
        AS newData
ON DUPLICATE KEY UPDATE status             = newData.status,
                        remaining_sessions = newData.remaining_sessions;

-- 생성된 계약(id=1)에 대한 첫 번째 PT 수업 예약
INSERT INTO pt_appointment (id, contract_id, status, start_time, end_time)
    VALUES (1, 1, 'SCHEDULED', '2025-08-22 10:00:00', '2025-08-22 11:00:00')
        AS newData
ON DUPLICATE KEY UPDATE status     = newData.status,
                        start_time = newData.start_time;

-- 회원 '김철수'(id=1)의 PT 수업(appointment_id=1)에 대한 운동일지 생성
INSERT INTO workout_log (id, member_id, workout_date) VALUES (1, 1, '2025-08-22') AS newData
ON DUPLICATE KEY UPDATE workout_date=newData.workout_date;
INSERT INTO workout_exercise (id, workout_log_id, exercise_id, log_order) VALUES (1, 1, 1, 1) AS newData
ON DUPLICATE KEY UPDATE log_order=newData.log_order; -- 벤치프레스
INSERT INTO workout_set (id, workout_exercise_id, set_order, weight, reps) VALUES (1, 1, 1, 80.0, 10) AS newData
ON DUPLICATE KEY UPDATE weight=newData.weight;

-- 위에서 생성된 운동일지(id=1)와 PT예약(id=1)을 PT세션으로 연결
INSERT INTO pt_session (id, workout_log_id, appointment_id) VALUES (1, 1, 1) AS newData
ON DUPLICATE KEY UPDATE workout_log_id=newData.workout_log_id;


-- ########## 8. 마스터 데이터 버전 초기화 ##########
INSERT INTO master_data_version (data_type, version, updated_at)
    VALUES ('EXERCISE', 1, NOW()),
           ('MUSCLE', 1, NOW()),
           ('EXERCISE_TARGET_MUSCLE', 1, NOW())
        AS newData
ON DUPLICATE KEY UPDATE version    = newData.version,
                        updated_at = newData.updated_at;