-- V1__Create_initial_tables.sql

-- 헬스장 정보 테이블
CREATE TABLE IF NOT EXISTS gym
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    address      VARCHAR(255),
    phone_number VARCHAR(255),
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 사용자/트레이너 통합 테이블
CREATE TABLE IF NOT EXISTS `member`
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    gym_id         BIGINT                                   NOT NULL,
    name           VARCHAR(255)                             NOT NULL,
    email          VARCHAR(255)                             NOT NULL UNIQUE,
    password       VARCHAR(255)                             NOT NULL,
    gender         ENUM ('MALE', 'FEMALE')                  NOT NULL,
    account_status ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED') NOT NULL,
    role           VARCHAR(31)                              NOT NULL,
    created_at     TIMESTAMP                                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    phone_number   VARCHAR(50),
    introduction   TEXT,

    CONSTRAINT fk_member_gym FOREIGN KEY (gym_id) REFERENCES gym (id),
    CONSTRAINT chk_member_role CHECK (role IN ('MEMBER', 'TRAINER', 'ADMIN'))
);

-- 체성분 정보 테이블
CREATE TABLE IF NOT EXISTS body_composition
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id        BIGINT NOT NULL,
    measurement_date DATE   NOT NULL,
    weight_kg        BIGINT,
    fat_kg           BIGINT,
    muscle_mass_kg   BIGINT,
    CONSTRAINT fk_body_composition_member FOREIGN KEY (member_id) REFERENCES `member` (id) ON DELETE CASCADE
);

-- 트레이너 학력
CREATE TABLE IF NOT EXISTS education
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id      BIGINT       NOT NULL,
    school_name    VARCHAR(255) NOT NULL,
    education_name VARCHAR(255),
    degree         VARCHAR(100),
    start_date     DATE,
    end_date       DATE,
    CONSTRAINT fk_education_member FOREIGN KEY (member_id) REFERENCES `member` (id) ON DELETE CASCADE
);


-- 트레이너 경력
CREATE TABLE IF NOT EXISTS work_experience
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id     BIGINT       NOT NULL,
    work_name     VARCHAR(255) NOT NULL,
    work_place    VARCHAR(255),
    work_position VARCHAR(255),
    work_start    DATE,
    work_end      DATE,
    CONSTRAINT fk_work_experience_member FOREIGN KEY (member_id) REFERENCES `member` (id) ON DELETE CASCADE
);

-- 트레이너 수상 경력
CREATE TABLE IF NOT EXISTS award
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id   BIGINT       NOT NULL,
    award_name  VARCHAR(255) NOT NULL,
    award_date  DATE,
    award_place VARCHAR(255),
    CONSTRAINT fk_award_member FOREIGN KEY (member_id) REFERENCES `member` (id) ON DELETE CASCADE
);

-- 트레이너 자격증
CREATE TABLE IF NOT EXISTS certification
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id            BIGINT       NOT NULL,
    certification_name   VARCHAR(255) NOT NULL,
    issuing_organization VARCHAR(255),
    acquisition_date     DATE,
    CONSTRAINT fk_certification_member FOREIGN KEY (member_id) REFERENCES `member` (id) ON DELETE CASCADE
);

-- 전문 분야 마스터 테이블
CREATE TABLE IF NOT EXISTS specialty
(
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- 트레이너와 전문 분야 매핑 테이블
CREATE TABLE IF NOT EXISTS trainer_specialty
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id    BIGINT NOT NULL,
    specialty_id BIGINT NOT NULL,
    CONSTRAINT fk_ts_member FOREIGN KEY (member_id) REFERENCES `member` (id) ON DELETE CASCADE,
    CONSTRAINT fk_ts_specialty FOREIGN KEY (specialty_id) REFERENCES specialty (id) ON DELETE CASCADE,
    UNIQUE (member_id, specialty_id)
);


-- 근육 정보 마스터 테이블
CREATE TABLE IF NOT EXISTS muscle
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(255) NOT NULL UNIQUE,
    korean_name  VARCHAR(255) NOT NULL UNIQUE,
    muscle_group VARCHAR(255) NOT NULL
);

-- 운동 정보 마스터 테이블
CREATE TABLE IF NOT EXISTS exercise
(
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- 운동과 근육의 관계 테이블 (매핑 테이블)
CREATE TABLE IF NOT EXISTS exercise_target_muscle
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    exercise_id      BIGINT      NOT NULL,
    target_muscle_id BIGINT      NOT NULL,
    muscle_role      VARCHAR(50) NOT NULL,
    CONSTRAINT fk_etm_exercise FOREIGN KEY (exercise_id) REFERENCES exercise (id) ON DELETE CASCADE,
    CONSTRAINT fk_etm_muscle FOREIGN KEY (target_muscle_id) REFERENCES muscle (id) ON DELETE CASCADE,
    UNIQUE (exercise_id, target_muscle_id)
);

-- 운동 일지 테이블 (계층 구조의 최상위)
CREATE TABLE IF NOT EXISTS workout_log
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id    BIGINT    NOT NULL,
    workout_date DATE      NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_log_member FOREIGN KEY (member_id) REFERENCES `member` (id) ON DELETE CASCADE
);

-- 운동 기록 테이블 (계층 구조의 중간)
CREATE TABLE IF NOT EXISTS workout_exercise
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    workout_log_id BIGINT    NOT NULL,
    exercise_id    BIGINT    NOT NULL,
    log_order      INT       NOT NULL,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_we_log FOREIGN KEY (workout_log_id) REFERENCES workout_log (id) ON DELETE CASCADE,
    CONSTRAINT fk_we_exercise FOREIGN KEY (exercise_id) REFERENCES exercise (id) ON DELETE CASCADE
);

-- 운동 세트 정보 테이블 (계층 구조의 최하위)
CREATE TABLE IF NOT EXISTS workout_set
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    workout_exercise_id BIGINT         NOT NULL,
    set_order           INT            NOT NULL,
    weight              DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    reps                INT            NOT NULL,
    CONSTRAINT fk_set_wo_exercise FOREIGN KEY (workout_exercise_id) REFERENCES workout_exercise (id) ON DELETE CASCADE
);

-- 피드백/메모 테이블
CREATE TABLE IF NOT EXISTS feedback
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_id           BIGINT    NOT NULL,
    content             TEXT      NOT NULL,
    workout_log_id      BIGINT,
    workout_exercise_id BIGINT,
    workout_set_id      BIGINT,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedback_author FOREIGN KEY (author_id) REFERENCES `member` (id) ON DELETE CASCADE,
    CONSTRAINT fk_feedback_log FOREIGN KEY (workout_log_id) REFERENCES workout_log (id) ON DELETE CASCADE,
    CONSTRAINT fk_feedback_exercise FOREIGN KEY (workout_exercise_id) REFERENCES workout_exercise (id) ON DELETE CASCADE,
    CONSTRAINT fk_feedback_set FOREIGN KEY (workout_set_id) REFERENCES workout_set (id) ON DELETE CASCADE,
    -- 피드백은 운동일지, 운동, 세트 중 하나에만 속해야 함
    CONSTRAINT chk_feedback_owner CHECK (
        (CASE WHEN workout_log_id IS NOT NULL THEN 1 ELSE 0 END) +
        (CASE WHEN workout_exercise_id IS NOT NULL THEN 1 ELSE 0 END) +
        (CASE WHEN workout_set_id IS NOT NULL THEN 1 ELSE 0 END) = 1
        )
);

-- 루틴 테이블
CREATE TABLE IF NOT EXISTS routine
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id   BIGINT       NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_routine_member FOREIGN KEY (member_id) REFERENCES `member` (id) ON DELETE CASCADE
);

-- 루틴 운동 테이블
CREATE TABLE IF NOT EXISTS routine_exercise
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    routine_id    BIGINT    NOT NULL,
    exercise_id   BIGINT    NOT NULL,
    routine_order INT       NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_re_routine FOREIGN KEY (routine_id) REFERENCES routine (id) ON DELETE CASCADE,
    CONSTRAINT fk_re_exercise FOREIGN KEY (exercise_id) REFERENCES exercise (id) ON DELETE CASCADE
);

-- 루틴 세트 테이블
CREATE TABLE IF NOT EXISTS routine_set
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    routine_exercise_id BIGINT         NOT NULL,
    weight              DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    reps                INT            NOT NULL,
    set_order           INT            NOT NULL,
    CONSTRAINT fk_rs_routine_exercise FOREIGN KEY (routine_exercise_id) REFERENCES routine_exercise (id) ON DELETE CASCADE
);

-- 마스터 데이터 버전 관리 테이블
CREATE TABLE IF NOT EXISTS master_data_version
(
    data_type  VARCHAR(255) PRIMARY KEY,
    version    VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE files (
  id bigint NOT NULL AUTO_INCREMENT,
  member_id bigint NOT NULL,
  file_path varchar(255) DEFAULT NULL,
  file_size bigint DEFAULT NULL,
  file_type varchar(255) DEFAULT NULL,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY fk_files_member (member_id),
  CONSTRAINT fk_files_member FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE
);

-- 성능 최적화를 위한 인덱스 추가
CREATE INDEX idx_member_gym_id ON `member` (gym_id);
CREATE INDEX idx_workout_log_member_date ON workout_log (member_id, workout_date);
CREATE INDEX idx_routine_member_id ON routine (member_id);
CREATE INDEX idx_body_composition_member_date ON body_composition (member_id, measurement_date);
