-- 테스트 유저 (OAuth 없이 직접 시딩)
INSERT INTO users (email, nickname, provider, provider_user_id, role, created_at, updated_at)
VALUES ('test@test.com', '테스터', 'GOOGLE', 'google-test-001', 'USER', NOW(), NOW());

-- 테스트 챌린지
INSERT INTO challenges (title, description, start_at, end_at, status, created_at, updated_at)
VALUES ('테스트 챌린지', '카프카 파이프라인 테스트용', NOW(), NOW() + INTERVAL '30 days', 'ACTIVE', NOW(), NOW());

-- 테스트 참여 (user_id=1, challenge_id=1)
INSERT INTO participations (user_id, challenge_id, total_points, created_at, updated_at)
VALUES (1, 1, 0, NOW(), NOW());
