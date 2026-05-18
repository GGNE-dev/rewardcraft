-- ── User BC 시드 데이터 ─────────────────────────────────────────────────────
-- 테스트 유저 (OAuth 없이 직접 시딩 — 개발 환경 전용)
-- id=1: 일반 사용자 (USER) — 리워드 교환, 미션 완료 테스트용
INSERT INTO users (email, nickname, provider, provider_user_id, role, created_at, updated_at)
VALUES ('test@test.com', '테스터', 'GOOGLE', 'google-test-001', 'USER', NOW(), NOW());

-- id=2: 운영자 (OPERATOR) — PERM_CHALLENGE_CREATE 등 운영 권한 테스트용
INSERT INTO users (email, nickname, provider, provider_user_id, role, created_at, updated_at)
VALUES ('operator@test.com', '운영자', 'GOOGLE', 'google-test-002', 'OPERATOR', NOW(), NOW());

-- id=3: 관리자 (ADMIN) — PERM_AUDIT_READ, PERM_USER_BAN, PERM_OPERATOR_MANAGE 테스트용
INSERT INTO users (email, nickname, provider, provider_user_id, role, created_at, updated_at)
VALUES ('admin@test.com', '관리자', 'GOOGLE', 'google-test-003', 'ADMIN', NOW(), NOW());

-- ── Reward BC 시드 데이터 ────────────────────────────────────────────────────
-- id=1: 재고 있음, 100점 — 정상 교환 테스트용 (user_id=1: 200점 보유 → 성공)
INSERT INTO rewards (name, required_points, total_stock, remaining_stock, created_at, updated_at)
VALUES ('스타벅스 아메리카노', 100, 50, 50, NOW(), NOW());

-- id=2: 재고 있음, 200점 — 포인트 부족 테스트용 (user_id=2: 포인트 없음 → 실패)
INSERT INTO rewards (name, required_points, total_stock, remaining_stock, created_at, updated_at)
VALUES ('편의점 상품권 5000원', 200, 30, 30, NOW(), NOW());

-- id=3: 재고 0, 50점 — 품절 테스트용
INSERT INTO rewards (name, required_points, total_stock, remaining_stock, created_at, updated_at)
VALUES ('품절된 리워드', 50, 10, 0, NOW(), NOW());
