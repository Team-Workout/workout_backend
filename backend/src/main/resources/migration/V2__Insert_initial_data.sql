-- Flyway V2: 초기 마스터 데이터 및 샘플 데이터 삽입 (DML) - trainer 테이블 분리 버전

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
-- [수정] muscle_role의 'PRIMARY'를 ENUM 타입에 맞게 'MAIN'으로 변경
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


-- ########## 5. 일반 사용자 샘플 데이터 (user) ##########
-- [수정] TRAINER 역할을 제외한 USER, ADMIN 역할의 사용자만 user 테이블에 삽입
INSERT INTO `user` (id, gym_id, email, password, name, gender, account_status, role)
VALUES (1, 1, 'chulsoo.kim@example.com', '$2a$10$yS.tJ2a.AXsOM2wD19356uYkS/Fl2i4O12s5lR5h7OJGnNn4AAt/S', '김철수', 'MALE', 'ACTIVE', 'USER'),
       (3, 1, 'minsu.park@example.com', '$2a$10$yS.tJ2a.AXsOM2wD19356uYkS/Fl2i4O12s5lR5h7OJGnNn4AAt/S', '박민수', 'MALE', 'ACTIVE', 'ADMIN'),
       (4, 2, 'jisoo.seo@example.com', '$2a$10$yS.tJ2a.AXsOM2wD19356uYkS/Fl2i4O12s5lR5h7OJGnNn4AAt/S', '서지수', 'FEMALE', 'ACTIVE', 'USER'),
       (5, 2, 'jihye.choi@example.com', '$2a$10$yS.tJ2a.AXsOM2wD19356uYkS/Fl2i4O12s5lR5h7OJGnNn4AAt/S', '최지혜', 'FEMALE', 'SUSPENDED', 'USER')
ON DUPLICATE KEY UPDATE email = VALUES(email), password = VALUES(password), name = VALUES(name), account_status = VALUES(account_status);


-- ########## 6. 트레이너 샘플 데이터 (trainer) ##########
-- [수정] TRAINER 역할의 사용자는 trainer 테이블에 별도로 삽입
INSERT INTO trainer (id, gym_id, email, password, name, gender, introduction, phone_number)
VALUES (2, 1, 'younghee.lee@example.com', '$2a$10$yS.tJ2a.AXsOM2wD19356uYkS/Fl2i4O12s5lR5h7OJGnNn4AAt/S', '이영희', 'FEMALE', '10년 경력의 베테랑 트레이너입니다. 함께 건강한 몸을 만들어봐요!', '010-1234-5678')
ON DUPLICATE KEY UPDATE email = VALUES(email), password = VALUES(password), name = VALUES(name), introduction = VALUES(introduction);


-- ########## 7. 트레이너 프로필 샘플 데이터 (신규 추가) ##########
-- 전문 분야(Specialty) 마스터 데이터
INSERT INTO specialty (id, name)
VALUES (1, '다이어트'), (2, '재활운동'), (3, '근력증가'), (4, '바디프로필')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 트레이너 '이영희'(id=2)의 프로필 데이터
INSERT INTO education (trainer_id, school_name, education_name, degree, start_date, end_date)
VALUES (2, '부산대학교', '체육교육과', '학사', '2010-03-01', '2014-02-20');

INSERT INTO work_experience (trainer_id, work_name, work_place, work_position, work_start, work_end)
VALUES (2, '파워 피트니스', '파워 피트니스 해운대점', '수석 트레이너', '2018-03-01', NULL);

INSERT INTO award (trainer_id, award_name, award_date, award_place)
VALUES (2, '전국 생활체육 보디빌딩 대회', '2017-10-15', '3위');

INSERT INTO certification (trainer_id, certification_name, issuing_organization, acquisition_date)
VALUES (2, '생활스포츠지도사 2급 (보디빌딩)', '국민체육진흥공단', '2015-12-01');

-- 트레이너 '이영희'의 전문분야 매핑
INSERT INTO trainer_specialty (trainer_id, specialty_id)
VALUES (2, 1), -- 다이어트
       (2, 2); -- 재활운동


-- ########## 8. 마스터 데이터 버전 초기화 ##########
INSERT INTO master_data_version (data_type, version, updated_at)
VALUES ('EXERCISE', '1.0.0', NOW()),
       ('MUSCLE', '1.0.0', NOW())
ON DUPLICATE KEY UPDATE version = VALUES(version), updated_at = VALUES(updated_at);