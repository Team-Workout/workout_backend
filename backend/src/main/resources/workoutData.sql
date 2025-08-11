-- ########## 1. 타겟 근육 마스터 데이터 (target_muscle) ##########
-- 이 데이터는 시스템의 모든 사용자가 공통으로 사용하는 근육 부위의 기준 정보입니다.
-- name 컬럼에 Enum의 이름(CHEST, BACK 등)을 직접 입력합니다.
INSERT INTO target_muscle (id, name) VALUES (1, 'CHEST');
INSERT INTO target_muscle (id, name) VALUES (2, 'BACK');
INSERT INTO target_muscle (id, name) VALUES (3, 'SHOULDERS');
INSERT INTO target_muscle (id, name) VALUES (4, 'BICEPS');
INSERT INTO target_muscle (id, name) VALUES (5, 'TRICEPS');
INSERT INTO target_muscle (id, name) VALUES (6, 'FOREARM');
INSERT INTO target_muscle (id, name) VALUES (7, 'ABS');
INSERT INTO target_muscle (id, name) VALUES (8, 'GLUTES');
INSERT INTO target_muscle (id, name) VALUES (9, 'QUADS');
INSERT INTO target_muscle (id, name) VALUES (10, 'HAMSTRINGS');
INSERT INTO target_muscle (id, name) VALUES (11, 'CALVES');


-- ########## 2. 운동 마스터 데이터 (exercise) ##########
-- 시스템에서 제공하는 기본 운동 목록입니다.
INSERT INTO exercise (id, name) VALUES (1, '벤치프레스');
INSERT INTO exercise (id, name) VALUES (2, '덤벨 프레스');
INSERT INTO exercise (id, name) VALUES (3, '인클라인 벤치프레스');
INSERT INTO exercise (id, name) VALUES (4, '딥스');
INSERT INTO exercise (id, name) VALUES (5, '푸시업');
INSERT INTO exercise (id, name) VALUES (6, '풀업');
INSERT INTO exercise (id, name) VALUES (7, '랫풀다운');
INSERT INTO exercise (id, name) VALUES (8, '바벨 로우');
INSERT INTO exercise (id, name) VALUES (9, '데드리프트');
INSERT INTO exercise (id, name) VALUES (10, '오버헤드 프레스');
INSERT INTO exercise (id, name) VALUES (11, '사이드 래터럴 레이즈');
INSERT INTO exercise (id, name) VALUES (12, '벤트 오버 래터럴 레이즈');
INSERT INTO exercise (id, name) VALUES (13, '스쿼트');
INSERT INTO exercise (id, name) VALUES (14, '레그 프레스');
INSERT INTO exercise (id, name) VALUES (15, '런지');
INSERT INTO exercise (id, name) VALUES (16, '바벨 컬');
INSERT INTO exercise (id, name) VALUES (17, '덤벨 컬');
INSERT INTO exercise (id, name) VALUES (18, '케이블 푸시다운');
INSERT INTO exercise (id, name) VALUES (19, '라잉 트라이셉스 익스텐션');
INSERT INTO exercise (id, name) VALUES (20, '크런치');
INSERT INTO exercise (id, name) VALUES (21, '레그 레이즈');


-- ########## 3. 운동-타겟 근육 관계 데이터 (exercise_target_muscle) ##########
-- 각 운동이 어떤 근육을 주동근(PRIMARY)으로 사용하고, 어떤 근육을 협력근(SECONDARY)으로 사용하는지 정의합니다.

-- 가슴 운동 (Chest)
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (1, 1, 'PRIMARY');   -- 벤치프레스 -> 가슴
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (1, 3, 'SECONDARY'); -- 벤치프레스 -> 어깨
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (1, 5, 'SECONDARY'); -- 벤치프레스 -> 삼두근
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (2, 1, 'PRIMARY');   -- 덤벨 프레스 -> 가슴
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (2, 3, 'SECONDARY'); -- 덤벨 프레스 -> 어깨
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (2, 5, 'SECONDARY'); -- 덤벨 프레스 -> 삼두근
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (3, 1, 'PRIMARY');   -- 인클라인 벤치프레스 -> 가슴
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (3, 3, 'SECONDARY'); -- 인클라인 벤치프레스 -> 어깨
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (4, 1, 'PRIMARY');   -- 딥스 -> 가슴
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (4, 5, 'PRIMARY');   -- 딥스 -> 삼두근
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (5, 1, 'PRIMARY');   -- 푸시업 -> 가슴
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (5, 5, 'SECONDARY'); -- 푸시업 -> 삼두근
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (5, 7, 'SECONDARY'); -- 푸시업 -> 복근

-- 등 운동 (Back)
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (6, 2, 'PRIMARY');   -- 풀업 -> 등
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (6, 4, 'SECONDARY'); -- 풀업 -> 이두근
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (7, 2, 'PRIMARY');   -- 랫풀다운 -> 등
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (7, 4, 'SECONDARY'); -- 랫풀다운 -> 이두근
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (8, 2, 'PRIMARY');   -- 바벨 로우 -> 등
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (8, 4, 'SECONDARY'); -- 바벨 로우 -> 이두근
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (9, 2, 'PRIMARY');   -- 데드리프트 -> 등
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (9, 8, 'PRIMARY');   -- 데드리프트 -> 둔근
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (9, 10, 'PRIMARY');  -- 데드리프트 -> 햄스트링

-- 어깨 운동 (Shoulders)
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (10, 3, 'PRIMARY');  -- 오버헤드 프레스 -> 어깨
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (10, 5, 'SECONDARY');-- 오버헤드 프레스 -> 삼두근
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (11, 3, 'PRIMARY');  -- 사이드 래터럴 레이즈 -> 어깨
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (12, 3, 'PRIMARY');  -- 벤트 오버 래터럴 레이즈 -> 어깨

-- 하체 운동 (Legs)
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (13, 9, 'PRIMARY');   -- 스쿼트 -> 대퇴사두근
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (13, 8, 'PRIMARY');   -- 스쿼트 -> 둔근
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (13, 10, 'SECONDARY');-- 스쿼트 -> 햄스트링
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (14, 9, 'PRIMARY');   -- 레그 프레스 -> 대퇴사두근
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (14, 8, 'PRIMARY');   -- 레그 프레스 -> 둔근
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (15, 9, 'PRIMARY');   -- 런지 -> 대퇴사두근
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (15, 8, 'PRIMARY');   -- 런지 -> 둔근

-- 팔 운동 (Arms)
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (16, 4, 'PRIMARY');  -- 바벨 컬 -> 이두근
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (17, 4, 'PRIMARY');  -- 덤벨 컬 -> 이두근
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (18, 5, 'PRIMARY');  -- 케이블 푸시다운 -> 삼두근
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (19, 5, 'PRIMARY');  -- 라잉 트라이셉스 익스텐션 -> 삼두근

-- 복근 운동 (Abs)
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (20, 7, 'PRIMARY');  -- 크런치 -> 복근
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES (21, 7, 'PRIMARY');  -- 레그 레이즈 -> 복근


-- ########## 4. 헬스장 마스터 데이터 (gym) ##########
INSERT INTO gym (id, name, address, phone_number) VALUES (1, '파워 피트니스', '부산광역시 해운대구', '051-123-4567');
INSERT INTO gym (id, name, address, phone_number) VALUES (2, '건강 제일 짐', '부산광역시 서면', '051-987-6543');


-- ########## 5. 사용자 데이터 (user) ##########
-- 사용자 데이터는 실제 운영 환경에서는 해시된 비밀번호를 저장해야 합니다. 여기서는 예시로 'password_hash'로 표기합니다.

-- 일반 사용자 (Normal User)
INSERT INTO user (id, gym_id, email, password, name, gender, account_status, created_at, role)
VALUES (1, 1, 'chulsoo.kim@example.com', 'hashed_password_1', '김철수', 'MALE', 'ACTIVE', '2025-08-01 10:00:00', 'USER');

-- 트레이너 (Trainer)
INSERT INTO user (id, gym_id, email, password, name, gender, account_status, created_at, role)
VALUES (2, 1, 'younghee.lee@example.com', 'hashed_password_2', '이영희', 'FEMALE', 'ACTIVE', '2025-08-01 11:00:00', 'TRAINER');

-- 관리자 (Admin)
INSERT INTO user (id, gym_id, email, password, name, gender, account_status, created_at, role)
VALUES (3, 1, 'minsu.park@example.com', 'hashed_password_3', '박민수', 'MALE', 'ACTIVE', '2025-07-20 09:00:00', 'ADMIN');

-- 다른 헬스장 소속 일반 사용자
INSERT INTO user (id, gym_id, email, password, name, gender, account_status, created_at, role)
VALUES (4, 2, 'jisoo.seo@example.com', 'hashed_password_4', '서지수', 'FEMALE', 'ACTIVE', '2025-08-05 15:00:00', 'USER');

-- 정지된 계정의 사용자 (Suspended User)
INSERT INTO user (id, gym_id, email, password, name, gender, account_status, created_at, role)
VALUES (5, 2, 'jihye.choi@example.com', 'hashed_password_5', '최지혜', 'FEMALE', 'SUSPENDED', '2025-06-15 14:00:00', 'USER');