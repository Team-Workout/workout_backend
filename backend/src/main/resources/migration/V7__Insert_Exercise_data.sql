-- ########## 추가 운동 데이터 (exercise) ##########
INSERT INTO exercise (id, name)
    VALUES (22, '페이스 풀'),
           (23, '케이블 로우'),
           (24, '덤벨 플라이'),
           (25, '케이블 크로스오버'),
           (26, '프론트 스쿼트'),
           (27, '레그 컬'),
           (28, '레그 익스텐션'),
           (29, '힙 쓰러스트'),
           (30, '덤벨 숄더 프레스'),
           (31, '해머 컬'),
           (32, '트라이셉스 킥백'),
           (33, '시티드 오버헤드 프레스'),
           (34, '스미스 머신 스쿼트'),
           (35, '카프 레이즈'),
           (36, '사이드 플랭크'),
           (37, '플랭크'),
           (38, '버피'),
           (39, '케틀벨 스윙'),
           (40, '체스트 딥스'),
           (41, '트레드밀'),
           (42, '사이클링')
        AS newData
ON DUPLICATE KEY UPDATE name = newData.name;

-- ########## 운동-타겟 근육 관계 데이터 (exercise_target_muscle) ##########
INSERT INTO exercise_target_muscle (exercise_id, target_muscle_id, muscle_role) VALUES
    -- 22. 페이스 풀
    (22, 2, 'PRIMARY'),   -- BACK (승모근, 능형근)
    (22, 3, 'PRIMARY'),   -- SHOULDERS (후삼각근)
    (22, 5, 'SECONDARY'), -- TRICEPS 약간 관여
    (22, 6, 'SECONDARY'), -- FOREARM(그립)

    -- 23. 케이블 로우
    (23, 2, 'PRIMARY'),   -- BACK (광배근, 능형근)
    (23, 3, 'SECONDARY'), -- SHOULDERS (후삼각근)
    (23, 4, 'SECONDARY'), -- BICEPS
    (23, 6, 'SECONDARY'), -- FOREARM

    -- 24. 덤벨 플라이
    (24, 1, 'PRIMARY'),   -- CHEST
    (24, 3, 'SECONDARY'), -- SHOULDERS (전면/측면)
    (24, 5, 'SECONDARY'), -- TRICEPS 안정화

    -- 25. 케이블 크로스오버
    (25, 1, 'PRIMARY'),   -- CHEST
    (25, 3, 'SECONDARY'), -- SHOULDERS
    (25, 5, 'SECONDARY'), -- TRICEPS

    -- 26. 프론트 스쿼트
    (26, 9, 'PRIMARY'),   -- QUADS
    (26, 8, 'SECONDARY'), -- GLUTES
    (26,10, 'SECONDARY'), -- HAMSTRINGS
    (26,11, 'SECONDARY'), -- CALVES
    (26, 7, 'SECONDARY'), -- ABS(코어)

    -- 27. 레그 컬
    (27,10, 'PRIMARY'),   -- HAMSTRINGS
    (27,11, 'SECONDARY'), -- CALVES

    -- 28. 레그 익스텐션
    (28, 9, 'PRIMARY'),   -- QUADS

    -- 29. 힙 쓰러스트
    (29, 8, 'PRIMARY'),   -- GLUTES
    (29,10, 'SECONDARY'), -- HAMSTRINGS
    (29, 9, 'SECONDARY'), -- QUADS 약간 관여

    -- 30. 덤벨 숄더 프레스
    (30, 3, 'PRIMARY'),   -- SHOULDERS
    (30, 5, 'SECONDARY'), -- TRICEPS
    (30, 6, 'SECONDARY'), -- FOREARM 안정화

    -- 31. 해머 컬
    (31, 4, 'PRIMARY'),   -- BICEPS (특히 상완근)
    (31, 6, 'SECONDARY'), -- FOREARM

    -- 32. 트라이셉스 킥백
    (32, 5, 'PRIMARY'),   -- TRICEPS
    (32, 6, 'SECONDARY'), -- FOREARM

    -- 33. 시티드 오버헤드 프레스
    (33, 3, 'PRIMARY'),   -- SHOULDERS
    (33, 5, 'SECONDARY'), -- TRICEPS
    (33, 6, 'SECONDARY'), -- FOREARM

    -- 34. 스미스 머신 스쿼트
    (34, 9, 'PRIMARY'),   -- QUADS
    (34, 8, 'SECONDARY'), -- GLUTES
    (34,10, 'SECONDARY'), -- HAMSTRINGS
    (34,11, 'SECONDARY'), -- CALVES
    (34, 7, 'SECONDARY'), -- ABS

    -- 35. 카프 레이즈
    (35,11, 'PRIMARY'),   -- CALVES

    -- 36. 사이드 플랭크
    (36, 7, 'PRIMARY'),   -- ABS (특히 외복사근)
    (36, 8, 'SECONDARY'), -- GLUTES (중둔근 안정화)

    -- 37. 플랭크
    (37, 7, 'PRIMARY'),   -- ABS (코어 전반)
    (37, 8, 'SECONDARY'), -- GLUTES
    (37, 2, 'SECONDARY'), -- BACK (척추기립근 안정화)

    -- 38. 버피
    (38, 7, 'PRIMARY'),   -- ABS
    (38, 9, 'PRIMARY'),   -- QUADS
    (38, 8, 'SECONDARY'), -- GLUTES
    (38,10, 'SECONDARY'), -- HAMSTRINGS
    (38,11, 'SECONDARY'), -- CALVES
    (38, 3, 'SECONDARY'), -- SHOULDERS

    -- 39. 케틀벨 스윙
    (39, 8, 'PRIMARY'),   -- GLUTES
    (39,10, 'PRIMARY'),   -- HAMSTRINGS
    (39, 7, 'SECONDARY'), -- ABS
    (39, 9, 'SECONDARY'), -- QUADS

    -- 40. 체스트 딥스
    (40, 1, 'PRIMARY'),   -- CHEST (하부 흉근)
    (40, 5, 'PRIMARY'),   -- TRICEPS
    (40, 3, 'SECONDARY'), -- SHOULDERS (전면)

    -- 41. 트레드밀
    (41, 9, 'PRIMARY'),   -- QUADS
    (41,10, 'PRIMARY'),   -- HAMSTRINGS
    (41,11, 'PRIMARY'),   -- CALVES
    (41, 8, 'SECONDARY'), -- GLUTES

    -- 42. 사이클링
    (42, 9, 'PRIMARY'),   -- QUADS
    (42,11, 'SECONDARY'), -- CALVES
    (42,10, 'SECONDARY'), -- HAMSTRINGS
    (42, 8, 'SECONDARY') -- GLUTES
        AS newData
ON DUPLICATE KEY UPDATE muscle_role = newData.muscle_role;

