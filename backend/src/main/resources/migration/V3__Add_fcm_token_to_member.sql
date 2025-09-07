-- V3__Add_fcm_token_to_member.sql
-- Member 엔티티의 fcmToken 필드에 대응하는 fcm_token 컬럼을 member 테이블에 추가합니다.

ALTER TABLE member
    ADD COLUMN fcm_token VARCHAR(255) NULL DEFAULT NULL COMMENT 'FCM 디바이스 등록 토큰' AFTER role;