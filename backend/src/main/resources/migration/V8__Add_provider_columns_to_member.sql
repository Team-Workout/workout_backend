-- V8__Add_provider_columns_to_member.sql
-- 소셜 로그인을 위한 provider와 provider_id 컬럼을 member 테이블에 추가합니다.
ALTER TABLE member
    ADD COLUMN provider VARCHAR(255) NULL,
    ADD COLUMN provider_id VARCHAR(255) NULL;

-- 동일한 소셜 계정이 중복으로 가입되는 것을 방지하기 위해 UNIQUE 인덱스를 추가합니다.
CREATE UNIQUE INDEX uk_member_provider_provider_id ON member (provider, provider_id);