-- Flyway V2: 초기 마스터 데이터 및 샘플 데이터 삽입 (DML) - 최종 수정본

-- ########## 1. 헬스장 마스터 데이터 (gym) ##########
INSERT INTO gym (id, name, address, phone_number)
VALUES (1, '파워 피트니스', '부산광역시 해운대구', '051-123-4567'),
       (2, '건강 제일 짐', '부산광역시 서면', '051-987-6543')
ON DUPLICATE KEY UPDATE name         = VALUES(name),
                        address      = VALUES(address),
                        phone_number = VALUES(phone_number);

-- ########## 2. 타겟 근육 마스터 데이터 (muscle) ##########
INSERT INTO muscle (id, name, korean_name, muscle_group)
VALUES (1, 'CHEST', '가슴', 'CHEST'),
       (2, 'BACK', '등', 'BACK'),
       (3, 'SHOULDERS', '어깨', 'SHOULDER'),
       (4, 'BICEPS', '이두', 'ARMS'),        -- ARMS 그룹으로 할당
       (5, 'TRICEPS', '삼두', 'ARMS'),        -- ARMS 그룹으로 할당
       (6, 'FOREARM', '전완근', 'ARMS'),      -- ARMS 그룹으로 할당
       (7, 'ABS', '복근', 'ABS'),
       (8, 'GLUTES', '둔근', 'LEGS'),         -- LEGS 그룹으로 할당
       (9, 'QUADS', '대퇴사두', 'LEGS'),      -- LEGS 그룹으로 할당
       (10, 'HAMSTRINGS', '햄스트링', 'LEGS'), -- LEGS 그룹으로 할당
       (11, 'CALVES', '종아리', 'LEGS')       -- LEGS 그룹으로 할당
ON DUPLICATE KEY UPDATE name        = VALUES(name),
                        korean_name = VALUES(korean_name),
                        muscle_group = VALUES(muscle_group);


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
ON DUPLICATE KEY UPDATE name = VALUES(name);


-- ########## 4. 운동-타겟 근육 관계 데이터 (exercise_target_muscle) ##########
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
ON DUPLICATE KEY UPDATE muscle_role = VALUES(muscle_role);


-- ########## 5. 사용자 샘플 데이터 (user) ##########
-- [수정] User.java 엔티티에 goal 필드가 없으므로, INSERT 문에서 해당 컬럼과 값을 제거하여 엔티티와 일치시켰습니다.
INSERT INTO `user` (id, gym_id, email, password, name, gender, account_status, role, created_at,
                    updated_at)
VALUES (1, 1, 'chulsoo.kim@example.com',
        '$2a$10$yS.tJ2a.AXsOM2wD19356uYkS/Fl2i4O12s5lR5h7OJGnNn4AAt/S', '김철수', 'MALE', 'ACTIVE',
        'USER', NOW(), NOW()),
       (2, 1, 'younghee.lee@example.com',
        '$2a$10$yS.tJ2a.AXsOM2wD19356uYkS/Fl2i4O12s5lR5h7OJGnNn4AAt/S', '이영희', 'FEMALE', 'ACTIVE',
        'TRAINER', NOW(), NOW()),
       (3, 1, 'minsu.park@example.com',
        '$2a$10$yS.tJ2a.AXsOM2wD19356uYkS/Fl2i4O12s5lR5h7OJGnNn4AAt/S', '박민수', 'MALE', 'ACTIVE',
        'ADMIN', NOW(), NOW()),
       (4, 2, 'jisoo.seo@example.com',
        '$2a$10$yS.tJ2a.AXsOM2wD19356uYkS/Fl2i4O12s5lR5h7OJGnNn4AAt/S', '서지수', 'FEMALE', 'ACTIVE',
        'USER', NOW(), NOW()),
       (5, 2, 'jihye.choi@example.com',
        '$2a$10$yS.tJ2a.AXsOM2wD19356uYkS/Fl2i4O12s5lR5h7OJGnNn4AAt/S', '최지혜', 'FEMALE',
        'SUSPENDED', 'USER', NOW(), NOW())
ON DUPLICATE KEY UPDATE gym_id         = VALUES(gym_id),
                        email          = VALUES(email),
                        password       = VALUES(password),
                        name           = VALUES(name),
                        gender         = VALUES(gender),
                        account_status = VALUES(account_status),
                        role           = VALUES(role),
                        updated_at     = NOW();


-- ########## 6. 마스터 데이터 버전 초기화 ##########
INSERT INTO master_data_version (data_type, version, updated_at)
VALUES ('EXERCISE', '1.0.0', NOW()),
       ('MUSCLE', '1.0.0', NOW())
ON DUPLICATE KEY UPDATE version    = VALUES(version),
                        updated_at = VALUES(updated_at);