-- ── Challenge BC 시드 데이터 ─────────────────────────────────────────────────
-- id=1: 활성 챌린지 — 운동 인증 (30일)
INSERT INTO challenges (title, description, start_at, end_at, status, created_at, updated_at)
VALUES ('30일 운동 챌린지', '매일 운동 인증으로 포인트를 쌓아보세요', NOW(), NOW() + INTERVAL '30 days', 'ACTIVE', NOW(), NOW());

-- id=2: 활성 챌린지 — 독서 인증 (60일)
INSERT INTO challenges (title, description, start_at, end_at, status, created_at, updated_at)
VALUES ('독서 인증 챌린지', '하루 30분 독서 인증 챌린지', NOW(), NOW() + INTERVAL '60 days', 'ACTIVE', NOW(), NOW());

-- ── Participation 시드 데이터 ─────────────────────────────────────────────────
-- MSA 경계: rewardcraft-api.users와 FK 없음 — userId만 보관
-- user_id=1(테스터) + challenge_id=1: 150점 — 리워드 교환 성공 테스트용 (100점 필요)
INSERT INTO participations (user_id, challenge_id, total_points, created_at, updated_at)
VALUES (1, 1, 150, NOW(), NOW());

-- user_id=1(테스터) + challenge_id=2: 50점 — 합산 200점 (두 챌린지 합산 조회 테스트)
INSERT INTO participations (user_id, challenge_id, total_points, created_at, updated_at)
VALUES (1, 2, 50, NOW(), NOW());

-- user_id=2(운영자) + challenge_id=1: 80점 — 참여 목록 조회 테스트용
INSERT INTO participations (user_id, challenge_id, total_points, created_at, updated_at)
VALUES (2, 1, 80, NOW(), NOW());
