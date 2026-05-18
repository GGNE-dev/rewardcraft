-- ── Challenge BC 시드 데이터 ─────────────────────────────────────────────────
INSERT INTO challenges (title, description, start_at, end_at, status, created_at, updated_at)
VALUES ('30일 운동 챌린지', '매일 운동 인증으로 포인트를 쌓아보세요', NOW(), NOW() + INTERVAL '30 days', 'ACTIVE', NOW(), NOW());

INSERT INTO challenges (title, description, start_at, end_at, status, created_at, updated_at)
VALUES ('독서 인증 챌린지', '하루 30분 독서 인증 챌린지', NOW(), NOW() + INTERVAL '60 days', 'ACTIVE', NOW(), NOW());

INSERT INTO challenges (title, description, start_at, end_at, status, created_at, updated_at)
VALUES ('코딩 챌린지', '매일 알고리즘 1문제 이상 풀기', NOW(), NOW() + INTERVAL '45 days', 'ACTIVE', NOW(), NOW());

-- ── Participation 시드 데이터 ─────────────────────────────────────────────────
-- MSA 경계: rewardcraft-api.users와 FK 없음 — userId만 보관
-- 챌린지별로 여러 참여자를 시딩해 랭킹 차트가 즉시 보이도록 함

-- [챌린지 1] 30일 운동
INSERT INTO participations (user_id, challenge_id, total_points, created_at, updated_at) VALUES (1, 1, 320, NOW(), NOW());
INSERT INTO participations (user_id, challenge_id, total_points, created_at, updated_at) VALUES (2, 1, 210, NOW(), NOW());
INSERT INTO participations (user_id, challenge_id, total_points, created_at, updated_at) VALUES (3, 1, 180, NOW(), NOW());
INSERT INTO participations (user_id, challenge_id, total_points, created_at, updated_at) VALUES (4, 1, 150, NOW(), NOW());
INSERT INTO participations (user_id, challenge_id, total_points, created_at, updated_at) VALUES (5, 1, 120, NOW(), NOW());
INSERT INTO participations (user_id, challenge_id, total_points, created_at, updated_at) VALUES (6, 1,  90, NOW(), NOW());
INSERT INTO participations (user_id, challenge_id, total_points, created_at, updated_at) VALUES (7, 1,  60, NOW(), NOW());

-- [챌린지 2] 독서 인증
INSERT INTO participations (user_id, challenge_id, total_points, created_at, updated_at) VALUES (1, 2, 270, NOW(), NOW());
INSERT INTO participations (user_id, challenge_id, total_points, created_at, updated_at) VALUES (3, 2, 200, NOW(), NOW());
INSERT INTO participations (user_id, challenge_id, total_points, created_at, updated_at) VALUES (5, 2, 140, NOW(), NOW());
INSERT INTO participations (user_id, challenge_id, total_points, created_at, updated_at) VALUES (7, 2,  80, NOW(), NOW());

-- [챌린지 3] 코딩 챌린지
INSERT INTO participations (user_id, challenge_id, total_points, created_at, updated_at) VALUES (2, 3, 400, NOW(), NOW());
INSERT INTO participations (user_id, challenge_id, total_points, created_at, updated_at) VALUES (4, 3, 350, NOW(), NOW());
INSERT INTO participations (user_id, challenge_id, total_points, created_at, updated_at) VALUES (6, 3, 290, NOW(), NOW());
INSERT INTO participations (user_id, challenge_id, total_points, created_at, updated_at) VALUES (1, 3, 240, NOW(), NOW());
INSERT INTO participations (user_id, challenge_id, total_points, created_at, updated_at) VALUES (3, 3, 190, NOW(), NOW());

-- ── MissionLog 시드 데이터 ─────────────────────────────────────────────────
-- participation_id는 위 INSERT 순서 기준 (auto_increment 1~16)
-- total_points 합계가 각 participation의 total_points와 일치해야 함
-- 실제 승인 플로우 시연을 위해 status = 'APPROVED'로 시딩

-- [참여 1] userId=1, 챌린지1 (30일 운동) → 320점
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (1, NOW() - INTERVAL '20 days', 100, '10km 러닝 완료', 'APPROVED', NOW() - INTERVAL '20 days', NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (1, NOW() - INTERVAL '15 days', 80,  '수영 1시간', 'APPROVED', NOW() - INTERVAL '15 days', NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (1, NOW() - INTERVAL '10 days', 70,  '자전거 30km', 'APPROVED', NOW() - INTERVAL '10 days', NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (1, NOW() - INTERVAL '5 days',  50,  '헬스 1시간', 'APPROVED', NOW() - INTERVAL '5 days',  NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (1, NOW() - INTERVAL '2 days',  20,  '스트레칭 30분', 'APPROVED', NOW() - INTERVAL '2 days', NOW());

-- [참여 2] userId=2, 챌린지1 → 210점
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (2, NOW() - INTERVAL '18 days', 100, '마라톤 완주', 'APPROVED', NOW() - INTERVAL '18 days', NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (2, NOW() - INTERVAL '8 days',  70,  '산악 등반', 'APPROVED', NOW() - INTERVAL '8 days',  NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (2, NOW() - INTERVAL '3 days',  40,  '줄넘기 500회', 'APPROVED', NOW() - INTERVAL '3 days', NOW());

-- [참여 3] userId=3, 챌린지1 → 180점
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (3, NOW() - INTERVAL '12 days', 100, '풀코스 마라톤', 'APPROVED', NOW() - INTERVAL '12 days', NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (3, NOW() - INTERVAL '6 days',  50,  '헬스 PT', 'APPROVED', NOW() - INTERVAL '6 days',  NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (3, NOW() - INTERVAL '1 days',  30,  '조깅 5km', 'APPROVED', NOW() - INTERVAL '1 days',  NOW());

-- [참여 4] userId=4, 챌린지1 → 150점
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (4, NOW() - INTERVAL '14 days', 100, '철인 3종 도전', 'APPROVED', NOW() - INTERVAL '14 days', NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (4, NOW() - INTERVAL '7 days',  30,  '수영 30분', 'APPROVED', NOW() - INTERVAL '7 days',  NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (4, NOW() - INTERVAL '2 days',  20,  '스쿼트 100회', 'APPROVED', NOW() - INTERVAL '2 days', NOW());

-- [참여 5] userId=5, 챌린지1 → 120점
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (5, NOW() - INTERVAL '10 days', 50, '배드민턴 2시간', 'APPROVED', NOW() - INTERVAL '10 days', NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (5, NOW() - INTERVAL '4 days',  50, '수영 1시간', 'APPROVED', NOW() - INTERVAL '4 days',  NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (5, NOW() - INTERVAL '1 days',  20, '스트레칭', 'APPROVED', NOW() - INTERVAL '1 days',  NOW());

-- [참여 6] userId=6, 챌린지1 → 90점
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (6, NOW() - INTERVAL '9 days', 50, '조깅 8km', 'APPROVED', NOW() - INTERVAL '9 days', NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (6, NOW() - INTERVAL '3 days', 40, '자전거 20km', 'APPROVED', NOW() - INTERVAL '3 days', NOW());

-- [참여 7] userId=7, 챌린지1 → 60점
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (7, NOW() - INTERVAL '5 days', 30, '줄넘기 300회', 'APPROVED', NOW() - INTERVAL '5 days', NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (7, NOW() - INTERVAL '2 days', 30, '스쿼트 50회', 'APPROVED', NOW() - INTERVAL '2 days', NOW());

-- [참여 8] userId=1, 챌린지2 (독서 인증) → 270점
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (8, NOW() - INTERVAL '25 days', 100, '클린코드 독서 완료', 'APPROVED', NOW() - INTERVAL '25 days', NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (8, NOW() - INTERVAL '16 days', 100, '리팩터링 2판', 'APPROVED', NOW() - INTERVAL '16 days', NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (8, NOW() - INTERVAL '7 days',   70, '오브젝트 1~5장', 'APPROVED', NOW() - INTERVAL '7 days', NOW());

-- [참여 9] userId=3, 챌린지2 → 200점
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (9, NOW() - INTERVAL '20 days', 100, '데브옵스 핸드북', 'APPROVED', NOW() - INTERVAL '20 days', NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (9, NOW() - INTERVAL '8 days',   60, 'AWS 입문서 완독', 'APPROVED', NOW() - INTERVAL '8 days', NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (9, NOW() - INTERVAL '2 days',   40, '카프카 핵심 가이드 3장', 'APPROVED', NOW() - INTERVAL '2 days', NOW());

-- [참여 10] userId=5, 챌린지2 → 140점
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (10, NOW() - INTERVAL '15 days', 100, '자바 ORM 표준 JPA', 'APPROVED', NOW() - INTERVAL '15 days', NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (10, NOW() - INTERVAL '4 days',   40, '스프링 핵심 원리 1~3장', 'APPROVED', NOW() - INTERVAL '4 days', NOW());

-- [참여 11] userId=7, 챌린지2 → 80점
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (11, NOW() - INTERVAL '10 days', 50, '모던 자바 인 액션', 'APPROVED', NOW() - INTERVAL '10 days', NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (11, NOW() - INTERVAL '3 days',  30, '이펙티브 자바 1장', 'APPROVED', NOW() - INTERVAL '3 days', NOW());

-- [참여 12] userId=2, 챌린지3 (코딩) → 400점
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (12, NOW() - INTERVAL '22 days', 100, '백준 골드4 달성', 'APPROVED', NOW() - INTERVAL '22 days', NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (12, NOW() - INTERVAL '14 days', 100, 'LeetCode Hard 5문제', 'APPROVED', NOW() - INTERVAL '14 days', NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (12, NOW() - INTERVAL '8 days',  100, '프로그래머스 Lv3 10문제', 'APPROVED', NOW() - INTERVAL '8 days', NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (12, NOW() - INTERVAL '2 days',  100, '코드포스 1600레이팅 도전', 'APPROVED', NOW() - INTERVAL '2 days', NOW());

-- [참여 13] userId=4, 챌린지3 → 350점
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (13, NOW() - INTERVAL '20 days', 100, '다익스트라 구현', 'APPROVED', NOW() - INTERVAL '20 days', NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (13, NOW() - INTERVAL '12 days', 100, '세그먼트 트리 완성', 'APPROVED', NOW() - INTERVAL '12 days', NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (13, NOW() - INTERVAL '5 days',  100, '네트워크 플로우 학습', 'APPROVED', NOW() - INTERVAL '5 days', NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (13, NOW() - INTERVAL '1 days',   50, '백트래킹 연습', 'APPROVED', NOW() - INTERVAL '1 days', NOW());

-- [참여 14] userId=6, 챌린지3 → 290점
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (14, NOW() - INTERVAL '18 days', 100, 'DP 유형 마스터', 'APPROVED', NOW() - INTERVAL '18 days', NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (14, NOW() - INTERVAL '9 days',  100, '그래프 탐색 완성', 'APPROVED', NOW() - INTERVAL '9 days', NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (14, NOW() - INTERVAL '3 days',   90, '투포인터 응용', 'APPROVED', NOW() - INTERVAL '3 days', NOW());

-- [참여 15] userId=1, 챌린지3 → 240점
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (15, NOW() - INTERVAL '17 days', 100, '스프링 시큐리티 구현', 'APPROVED', NOW() - INTERVAL '17 days', NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (15, NOW() - INTERVAL '10 days', 100, 'JWT 인증 직접 구현', 'APPROVED', NOW() - INTERVAL '10 days', NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (15, NOW() - INTERVAL '4 days',   40, 'QueryDSL 동적 쿼리', 'APPROVED', NOW() - INTERVAL '4 days', NOW());

-- [참여 16] userId=3, 챌린지3 → 190점
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (16, NOW() - INTERVAL '13 days', 100, 'Kafka 컨슈머 구현', 'APPROVED', NOW() - INTERVAL '13 days', NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (16, NOW() - INTERVAL '6 days',   50, 'Redis 분산락 적용', 'APPROVED', NOW() - INTERVAL '6 days', NOW());
INSERT INTO mission_logs (participation_id, completed_at, points_earned, memo, status, created_at, updated_at)
VALUES (16, NOW() - INTERVAL '2 days',   40, 'Docker Compose 구성', 'APPROVED', NOW() - INTERVAL '2 days', NOW());
