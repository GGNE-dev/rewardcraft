-- ── User BC 시드 데이터 ─────────────────────────────────────────────────────
-- id=1~3: 역할별 고정 계정 — DevController /dev/token/{id} 로 JWT 발급 가능
INSERT INTO users (email, nickname, provider, provider_user_id, role, created_at, updated_at)
VALUES ('test@test.com', '테스터_1', 'GOOGLE', 'google-test-001', 'USER', NOW(), NOW());

INSERT INTO users (email, nickname, provider, provider_user_id, role, created_at, updated_at)
VALUES ('operator@test.com', '운영자', 'GOOGLE', 'google-test-002', 'OPERATOR', NOW(), NOW());

INSERT INTO users (email, nickname, provider, provider_user_id, role, created_at, updated_at)
VALUES ('admin@test.com', '관리자', 'GOOGLE', 'google-test-003', 'ADMIN', NOW(), NOW());

-- id=4~7: 랭킹 시딩용 더미 유저 (challenge-service의 participation user_id=4~7과 매핑)
INSERT INTO users (email, nickname, provider, provider_user_id, role, created_at, updated_at)
VALUES ('user4@test.com', '달리기왕', 'GOOGLE', 'google-test-004', 'USER', NOW(), NOW());

INSERT INTO users (email, nickname, provider, provider_user_id, role, created_at, updated_at)
VALUES ('user5@test.com', '독서광', 'GOOGLE', 'google-test-005', 'USER', NOW(), NOW());

INSERT INTO users (email, nickname, provider, provider_user_id, role, created_at, updated_at)
VALUES ('user6@test.com', '코딩마스터', 'GOOGLE', 'google-test-006', 'USER', NOW(), NOW());

INSERT INTO users (email, nickname, provider, provider_user_id, role, created_at, updated_at)
VALUES ('user7@test.com', '꾸준함의달인', 'GOOGLE', 'google-test-007', 'USER', NOW(), NOW());

-- ── Reward BC 시드 데이터 ────────────────────────────────────────────────────
-- id=1: 100점 — 시딩 유저(id=1)가 320점 보유하므로 즉시 교환 가능
INSERT INTO rewards (name, required_points, total_stock, remaining_stock, created_at, updated_at)
VALUES ('스타벅스 아메리카노', 100, 50, 50, NOW(), NOW());

-- id=2: 200점
INSERT INTO rewards (name, required_points, total_stock, remaining_stock, created_at, updated_at)
VALUES ('편의점 상품권 5000원', 200, 30, 30, NOW(), NOW());

-- id=3: 500점 — 고가 리워드 (포인트 부족 테스트)
INSERT INTO rewards (name, required_points, total_stock, remaining_stock, created_at, updated_at)
VALUES ('치킨 상품권', 500, 20, 20, NOW(), NOW());

-- id=4: 50점, 재고 3 — 품절 임박 테스트
INSERT INTO rewards (name, required_points, total_stock, remaining_stock, created_at, updated_at)
VALUES ('음료 쿠폰', 50, 10, 3, NOW(), NOW());

-- id=5: 재고 0 — 품절 UI 테스트
INSERT INTO rewards (name, required_points, total_stock, remaining_stock, created_at, updated_at)
VALUES ('품절된 한정판 굿즈', 300, 5, 0, NOW(), NOW());
