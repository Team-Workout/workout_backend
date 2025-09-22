-- ########## 1. 피드 (Feed) 테이블 ##########
-- Feed.java 엔티티에 해당합니다.
CREATE TABLE IF NOT EXISTS feed
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '피드 ID',
    gym_id     BIGINT       NOT NULL COMMENT '헬스장 ID',
    member_id  BIGINT       NOT NULL COMMENT '작성자 회원 ID',
    image_url  VARCHAR(1000) NOT NULL COMMENT '피드 이미지 저장 경로',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',

    -- 외래 키 제약조건
    CONSTRAINT fk_feed_gym FOREIGN KEY (gym_id) REFERENCES gym (id) ON DELETE CASCADE,
    CONSTRAINT fk_feed_member FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE
) COMMENT '오운완 피드 게시물 테이블';

-- ########## 2. 댓글 (Comment) 테이블 ##########
-- Comment.java 엔티티에 해당합니다.
CREATE TABLE IF NOT EXISTS comment
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '댓글 ID',
    feed_id           BIGINT       NOT NULL COMMENT '댓글이 달린 최상위 피드 ID',
    member_id         BIGINT       NOT NULL COMMENT '댓글 작성자 ID',
    parent_comment_id BIGINT NULL COMMENT '부모 댓글 ID (대댓글용)',
    content           TEXT         NOT NULL COMMENT '댓글 내용',
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',

    -- 외래 키 제약조건
    CONSTRAINT fk_comment_feed FOREIGN KEY (feed_id) REFERENCES feed (id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_member FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_comment_id) REFERENCES comment (id) ON DELETE CASCADE
) COMMENT '피드 및 댓글에 대한 댓글 테이블';


-- ########## 3. 좋아요 (Likes) 테이블 ##########
-- Like.java 엔티티에 해당합니다.
CREATE TABLE IF NOT EXISTS likes
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '좋아요 ID',
    member_id   BIGINT       NOT NULL COMMENT '좋아요를 누른 회원 ID',
    target_type VARCHAR(255) NOT NULL COMMENT '좋아요 대상 타입 (FEED, COMMENT)',
    target_id   BIGINT       NOT NULL COMMENT '좋아요 대상의 ID',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',

    -- 외래 키 제약조건
    CONSTRAINT fk_likes_member FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE,
    -- 복합 유니크 제약조건: 한 명의 유저는 하나의 대상에 한 번만 좋아요를 누를 수 있습니다.
    CONSTRAINT uk_likes_member_target UNIQUE (member_id, target_type, target_id)
) COMMENT '피드 및 댓글에 대한 좋아요 테이블';


-- ########## 4. 성능 최적화를 위한 인덱스 추가 ##########
-- 피드 목록 조회를 빠르게 하기 위한 인덱스 (헬스장 ID와 생성 시간 기준)
CREATE INDEX idx_feed_gym_id_created_at ON feed (gym_id, created_at);

-- 특정 피드의 댓글 조회를 빠르게 하기 위한 인덱스
CREATE INDEX idx_comment_feed_id ON comment (feed_id);

-- 특정 댓글의 대댓글 조회를 빠르게 하기 위한 인덱스
CREATE INDEX idx_comment_parent_comment_id ON comment (parent_comment_id);

-- 특정 대상의 좋아요 수를 빠르게 계산하기 위한 인덱스
CREATE INDEX idx_likes_target ON likes (target_type, target_id);