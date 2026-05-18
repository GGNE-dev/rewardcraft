# RewardCraft 종합 테스트 시나리오 (Ch 00–06)

> **목적**: Ch 00–06까지 구현된 모든 기능이 MSA 환경에서 정상 동작하는지 검증한다.
> 테스트는 순서대로 실행한다. 앞 단계의 결과(토큰, ID 등)를 다음 단계에서 사용한다.

---

## 사전 준비

### 1. 시드 데이터 구성 (서비스 기동 시 자동 삽입)

| 서비스 | ID | 항목 | 비고 |
|--------|-----|------|------|
| rewardcraft-api | users.id=1 | 테스터 (USER 역할) | 리워드 교환 메인 테스트 계정 |
| rewardcraft-api | users.id=2 | 운영자 (OPERATOR 역할) | RBAC 테스트 계정 |
| rewardcraft-api | users.id=3 | 관리자 (ADMIN 역할) | 감사 로그, 운영자 관리 테스트 |
| rewardcraft-api | rewards.id=1 | 스타벅스 아메리카노 (100점, 재고 50) | 교환 성공 테스트 |
| rewardcraft-api | rewards.id=2 | 편의점 상품권 (200점, 재고 30) | 포인트 부족 테스트 |
| rewardcraft-api | rewards.id=3 | 품절된 리워드 (50점, 재고 0) | 품절 테스트 |
| challenge-service | challenges.id=1 | 30일 운동 챌린지 (ACTIVE) | 미션 완료 메인 테스트 |
| challenge-service | challenges.id=2 | 독서 인증 챌린지 (ACTIVE) | 다중 참여 테스트 |
| challenge-service | participations.id=1 | user_id=1, challenge_id=1, 150점 | Feign 포인트 조회 테스트 |
| challenge-service | participations.id=2 | user_id=1, challenge_id=2, 50점 | 합산 포인트 = 200점 |
| challenge-service | participations.id=3 | user_id=2, challenge_id=1, 80점 | 운영자 참여 목록 조회 |

**포인트 계산**:
- user_id=1 전체 합산: 150 + 50 = **200점** (스타벅스 아메리카노 100점 교환 가능)
- user_id=2 전체 합산: 80점 (편의점 상품권 200점 교환 불가)

---

### 2. 서비스 기동 순서

```bash
# 터미널 1 — 인프라
docker compose up -d
docker compose ps   # postgres, redis, kafka 모두 healthy 확인

# 터미널 2 — challenge-service (포트 8082)
./gradlew :challenge-service:bootRun

# 터미널 3 — rewardcraft-api (포트 8080)
./gradlew :rewardcraft-api:bootRun

# 터미널 4 — notification-service (포트 8081)
./gradlew :notification-service:bootRun
```

**기동 확인 로그:**
- challenge-service: `Started ChallengeServiceApplication`
- rewardcraft-api: `Started RewardcraftApplication`
- notification-service: `Started NotificationServiceApplication`

---

### 3. 테스트 토큰 발급 (모든 테스트 시작 전)

`/api/dev/**`는 `local` 프로파일 전용이며 인증 없이 호출 가능하다.

```
POST http://localhost:8080/api/dev/token?userId=1
→ USER_TOKEN (테스터)

POST http://localhost:8080/api/dev/token?userId=2
→ OPERATOR_TOKEN (운영자)

POST http://localhost:8080/api/dev/token?userId=3
→ ADMIN_TOKEN (관리자)
```

이후 모든 요청에서:
- `Authorization: Bearer {USER_TOKEN}` 형식으로 헤더에 첨부
- 응답의 `data.accessToken` 값을 사용

---

## TC-01 ~ TC-03: 사용자 기본 CRUD (Ch 01 — JPA)

### TC-01: 사용자 단건 조회

```
GET http://localhost:8080/api/users/1
Authorization: Bearer {USER_TOKEN}
```

**예상 응답 (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "test@test.com",
    "nickname": "테스터",
    "role": "USER"
  }
}
```

**확인 포인트**: `email`, `nickname`, `role` 값이 시드 데이터와 일치하는지 확인.

---

### TC-02: 닉네임 수정 (Dirty Checking)

```
PATCH http://localhost:8080/api/users/1/nickname
Authorization: Bearer {USER_TOKEN}
Content-Type: application/json

{ "nickname": "수정된닉네임" }
```

**예상 응답 (200 OK):**
```json
{ "success": true, "data": { "id": 1, "nickname": "수정된닉네임" } }
```

**확인 포인트**:
- `explicit save()` 없이 Dirty Checking으로 DB 반영됐는지 확인
- `GET /api/users/1` 재호출 시 닉네임이 변경됐는지 확인

---

### TC-03: DevController — 토큰 재발급

```
POST http://localhost:8080/api/dev/token?userId=1
```

**예상 응답 (200 OK):**
```json
{ "success": true, "data": { "accessToken": "eyJ..." } }
```

**확인 포인트**: 발급된 토큰으로 `GET /api/users/1` 호출 시 인증 성공하는지 확인.

---

## TC-04 ~ TC-10: RBAC + Rate Limiting + Audit Log (Ch 03)

### TC-04: OPERATOR 전용 API 접근 성공

```
GET http://localhost:8080/api/admin/challenges
Authorization: Bearer {OPERATOR_TOKEN}
```

**예상 응답 (200 OK):**
```json
{ "success": true, "data": "챌린지 관리 접근 성공" }
```

**확인 포인트**: OPERATOR는 `PERM_CHALLENGE_CREATE` 보유 → 성공.

---

### TC-05: USER가 OPERATOR 전용 API 접근 — 403 Forbidden

```
GET http://localhost:8080/api/admin/challenges
Authorization: Bearer {USER_TOKEN}
```

**예상 응답 (403):**
```json
{ "success": false, "message": "접근 권한이 없습니다." }
```

**확인 포인트**: USER는 `PERM_CHALLENGE_CREATE` 미보유 → `@PreAuthorize` 차단.

---

### TC-06: ADMIN 전용 API — ADMIN 접근 성공

```
GET http://localhost:8080/api/admin/users/ban
Authorization: Bearer {ADMIN_TOKEN}
```

**예상 응답 (200 OK):**
```json
{ "success": true, "data": "사용자 정지 접근 성공" }
```

---

### TC-07: ADMIN 전용 API — OPERATOR 접근 실패

```
GET http://localhost:8080/api/admin/users/ban
Authorization: Bearer {OPERATOR_TOKEN}
```

**예상 응답 (403):** OPERATOR는 `PERM_USER_BAN` 미보유.

---

### TC-08: 감사 로그 조회 — @Audited AOP 동작 확인

```
GET http://localhost:8080/api/admin/audit-logs
Authorization: Bearer {ADMIN_TOKEN}
```

**예상 응답 (200 OK):** TC-04에서 `CHALLENGE_MANAGE_VIEW` 액션이 기록된 로그 포함.

```json
{
  "success": true,
  "data": [
    {
      "action": "CHALLENGE_MANAGE_VIEW",
      "targetType": "CHALLENGE",
      "targetId": "all",
      "actorId": 2
    }
  ]
}
```

**확인 포인트**: `@Audited` AOP가 OPERATOR(userId=2)의 접근을 자동으로 기록했는지 확인.

---

### TC-09: Rate Limiting — 횟수 초과 시 429

`/api/auth/refresh` 는 IP당 1분에 3회 제한 (`capacity=3`).

**PowerShell:**
```powershell
1..4 | ForEach-Object {
    $res = curl.exe -s -o NUL -w "%{http_code}" -X POST "http://localhost:8080/api/auth/refresh?refreshToken=invalid"
    Write-Host "$_ 번째: $res"
}
```

**예상 응답 순서:**
```
1 번째: 401   ← 토큰 유효하지 않음
2 번째: 401
3 번째: 401
4 번째: 429   ← Too Many Requests (Rate Limit 초과)
```

**확인 포인트**: 4번째 요청부터 `429`가 반환되는지 확인. 1분 후 재시도 시 다시 `401` 반환.

---

### TC-10: 인증 없는 요청 — 401

```
GET http://localhost:8080/api/users/1
(Authorization 헤더 없음)
```

**예상 응답 (401 Unauthorized)**.

---

## TC-11 ~ TC-14: Challenge 서비스 기본 CRUD (challenge-service)

### TC-11: 활성 챌린지 목록 조회 + Redis 캐시 확인

```
GET http://localhost:8082/api/challenges
```

**예상 응답 (200 OK):**
```json
{
  "success": true,
  "data": [
    { "id": 1, "title": "30일 운동 챌린지", "status": "ACTIVE" },
    { "id": 2, "title": "독서 인증 챌린지", "status": "ACTIVE" }
  ]
}
```

**캐시 확인 포인트**:
- 첫 번째 호출: challenge-service 콘솔에 `SELECT ... FROM challenges` 쿼리 출력
- 두 번째 호출: DB 쿼리 없음 (Redis `challenge::active`에 캐시됨)

---

### TC-12: 챌린지 신규 생성

```
POST http://localhost:8082/api/challenges
Content-Type: application/json

{
  "title": "테스트 챌린지",
  "description": "테스트용",
  "startAt": "2026-05-18T00:00:00",
  "endAt": "2026-06-18T00:00:00"
}
```

**예상 응답 (201 Created):**
```json
{ "success": true, "data": { "id": 3, "title": "테스트 챌린지", "status": "ACTIVE" } }
```

---

### TC-13: 챌린지 단건 조회

```
GET http://localhost:8082/api/challenges/1
```

**예상 응답 (200 OK):** id=1 챌린지 정보.

---

### TC-14: 챌린지 참여

```
POST http://localhost:8082/api/participations
Content-Type: application/json

{ "userId": 3, "challengeId": 1 }
```

**예상 응답 (201 Created):** participation 생성 확인.

**TC-14-B: 중복 참여 시도**
```
POST http://localhost:8082/api/participations
{ "userId": 1, "challengeId": 1 }
```
**예상 응답 (409):** `ALREADY_PARTICIPATED` — user_id=1은 이미 challenge_id=1에 참여 중.

---

## TC-15 ~ TC-18: 미션 완료 + Kafka 이벤트 (Ch 05)

### TC-15: 미션 완료 — Kafka 이벤트 발행 확인

```
POST http://localhost:8082/api/participations/1/missions
Content-Type: application/json

{ "points": 50, "memo": "오늘 운동 인증 완료" }
```

**예상 응답 (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "participationId": 1,
    "pointsEarned": 50,
    "memo": "오늘 운동 인증 완료"
  }
}
```

**확인 포인트 — challenge-service 콘솔:**
```
Kafka 발행 성공: eventId=xxx, partition=0, offset=0
```

**확인 포인트 — PointsConsumer (같은 challenge-service 내):**
```
이벤트 수신: eventId=xxx, partition=0, offset=0
```

**DB 확인**: `participations.total_points` id=1이 150 → **200**으로 증가.

---

### TC-16: Kafka — notification-service 수신 확인

TC-15 직후 notification-service 콘솔 확인:

```
[알림] 미션 완료 알림 발송: userId=1, points=50
```

**확인 포인트**: challenge-service가 발행한 이벤트를 notification-service가 별도 consumer group으로 독립 수신.

---

### TC-17: 미션 완료 이력 조회

```
GET http://localhost:8082/api/participations/1/missions
```

**예상 응답 (200 OK):** TC-15에서 완료한 미션 로그 포함.

---

### TC-18: Redis 랭킹 업데이트 확인

TC-15 (미션 완료) 후 PointsConsumer가 `ZADD ranking:challenge:1 50 "1"` 실행.

```
GET http://localhost:8082/api/challenges/1/ranking/top?limit=10
```

**예상 응답 (200 OK):**
```json
{
  "success": true,
  "data": [
    { "rank": 1, "userId": 1, "score": 50 }
  ]
}
```

**내 순위 조회:**
```
GET http://localhost:8082/api/challenges/1/ranking/me?userId=1
```

**예상 응답:**
```json
{ "success": true, "data": { "rank": 1, "score": 50 } }
```

**확인 포인트**: Kafka 이벤트 → PointsConsumer → `redisTemplate.opsForZSet().incrementScore()` 흐름.

---

## TC-19 ~ TC-24: Feign + CircuitBreaker + 분산 락 (Ch 04 + Ch 06)

### TC-19: 내부 API 직접 호출 — Feign 경로 확인

```
GET http://localhost:8082/internal/participants/points?userId=1
```

**예상 응답 (200 OK):** `200` (150+50 초기값, TC-15 이후라면 250)

이 엔드포인트는 rewardcraft-api의 Feign 클라이언트가 호출하는 경로와 동일하다.

---

### TC-20: 리워드 교환 — Feign 통신 + 분산 락 성공 케이스

```
POST http://localhost:8080/api/rewards/1/exchange
Authorization: Bearer {USER_TOKEN}
```

**예상 흐름:**
1. Redisson 분산 락 `lock:reward:1` 획득 (Redis)
2. `SELECT ... FOR UPDATE` — 스타벅스 아메리카노 조회 (재고 50 > 0)
3. Feign `GET http://localhost:8082/internal/participants/points?userId=1` → 200점 반환
4. 200 ≥ 100 (requiredPoints) → 교환 허용
5. `remaining_stock` 50 → 49
6. `reward_exchanges` 레코드 생성
7. 분산 락 해제

**예상 응답 (200 OK):**
```json
{
  "success": true,
  "data": { "exchangeId": 1, "remainingStock": 49 }
}
```

**확인 포인트**:
- rewardcraft-api 콘솔: Feign 호출 로그
- challenge-service 콘솔: `GET /internal/participants/points?userId=1` 수신 로그

---

### TC-21: 리워드 교환 — 포인트 부족 (400)

user_id=2(운영자)는 participation이 있지만 80점 보유. 편의점 상품권은 200점 필요.

```
POST http://localhost:8080/api/rewards/2/exchange
Authorization: Bearer {OPERATOR_TOKEN}
```

**예상 응답 (400 Bad Request):**
```json
{
  "success": false,
  "code": "RWD_003",
  "message": "포인트가 부족합니다."
}
```

---

### TC-22: 리워드 교환 — 품절 (409)

reward_id=3은 재고 0.

```
POST http://localhost:8080/api/rewards/3/exchange
Authorization: Bearer {USER_TOKEN}
```

**예상 응답 (409 Conflict):**
```json
{
  "success": false,
  "code": "RWD_002",
  "message": "해당 리워드가 품절됐습니다."
}
```

---

### TC-23: CircuitBreaker Fallback — challenge-service 다운 시 503

```
# 1단계: challenge-service 프로세스 중단 (IntelliJ Stop 버튼 또는 터미널 Ctrl+C)

# 2단계: 리워드 교환 시도 (PowerShell)
```
```powershell
$token = "여기에_USER_토큰_붙여넣기"
1..3 | ForEach-Object {
    $res = curl.exe -s -X POST "http://localhost:8080/api/rewards/1/exchange" `
        -H "Authorization: Bearer $token"
    Write-Host "$_ 번째: $res"
}
```

**예상 응답:**
- 초기 몇 회: Feign Timeout 후 FallbackFactory 실행 → **503**
- 10회 중 5회 이상 실패 → CircuitBreaker **Open 전환**
- 이후 요청: challenge-service 호출 없이 즉시 **503** (rewardcraft-api 로그 확인)

```json
{
  "success": false,
  "code": "SVC_001",
  "message": "챌린지 서비스에 일시적으로 접근할 수 없습니다. 잠시 후 다시 시도해주세요."
}
```

**확인 포인트 — rewardcraft-api 콘솔:**
```
[ERROR] challenge-service 호출 실패 (userId=1): ...
```

**3단계: challenge-service 재기동 후 60초 대기 → Half-Open → 정상 복구 확인**

---

### TC-24: 분산 락 — 동시 교환 요청 재고 정합성

동일 리워드에 동시 다발 요청 시 재고가 음수가 되지 않는지 검증.

**PowerShell — 10개 동시 요청:**
```powershell
$token = "여기에_USER_토큰_붙여넣기"

$jobs = 1..10 | ForEach-Object {
    Start-Job -ScriptBlock {
        param($tok)
        curl.exe -s -X POST "http://localhost:8080/api/rewards/1/exchange" `
            -H "Authorization: Bearer $tok"
    } -ArgumentList $token
}

$results = $jobs | Wait-Job | Receive-Job
$jobs | Remove-Job
$results | ForEach-Object { Write-Host $_ }
```

**예상 결과:**
- `remainingStock` 값이 중복 없이 순차적으로 감소 (예: 48→47→46→...→39)
- `remainingStock`이 음수가 되지 않음
- 일부 요청은 `429 LOCK_ACQUISITION_FAILED` 반환 가능 (3초 내 락 획득 실패 시)

**확인 방법**: 모든 요청 완료 후 `remaining_stock` 최종값이 0 이상인지 확인.

---

## TC-25 ~ TC-27: QueryDSL 고급 조회 (Ch 01)

### TC-25: 챌린지별 참여자 요약 (QueryDSL Projection)

```
GET http://localhost:8082/api/participations/1/summary
```

**예상 응답 (200 OK):**
```json
{
  "success": true,
  "data": [
    { "userId": 1, "totalPoints": 200, "missionCount": 1 },
    { "userId": 2, "totalPoints": 80, "missionCount": 0 }
  ]
}
```

**확인 포인트**: QueryDSL `Projections.constructor`로 userId + totalPoints + missionCount만 반환 (nickname 없음 — User BC 분리).

---

### TC-26: 사용자별 참여 목록 조회

```
GET http://localhost:8082/api/participations?userId=1
```

**예상 응답 (200 OK):**
```json
{
  "success": true,
  "data": [
    { "id": 1, "userId": 1, "challengeId": 1, "totalPoints": 200 },
    { "id": 2, "userId": 1, "challengeId": 2, "totalPoints": 50 }
  ]
}
```

---

### TC-27: 내부 포인트 합산 API (Feign 경로)

```
GET http://localhost:8082/internal/participants/points?userId=1
```

**예상 응답:** 전체 포인트 합산값 (TC-15 미션 완료 후라면 250, 이전이라면 200).

---

## 테스트 완료 체크리스트

| 분류 | 테스트 | 완료 |
|------|--------|------|
| **사용자** | TC-01 사용자 조회 | ☐ |
| **사용자** | TC-02 닉네임 수정 (Dirty Checking) | ☐ |
| **인증** | TC-03 Dev 토큰 발급 | ☐ |
| **RBAC** | TC-04 OPERATOR 접근 성공 | ☐ |
| **RBAC** | TC-05 USER → OPERATOR API 403 | ☐ |
| **RBAC** | TC-06 ADMIN 전용 API 성공 | ☐ |
| **RBAC** | TC-07 OPERATOR → ADMIN API 403 | ☐ |
| **Audit** | TC-08 감사 로그 @Audited 확인 | ☐ |
| **Rate Limit** | TC-09 429 Too Many Requests | ☐ |
| **인증** | TC-10 401 미인증 요청 | ☐ |
| **Challenge** | TC-11 목록 조회 + 캐시 확인 | ☐ |
| **Challenge** | TC-12 신규 생성 | ☐ |
| **Challenge** | TC-13 단건 조회 | ☐ |
| **Participation** | TC-14 참여 + 중복 참여 409 | ☐ |
| **Kafka** | TC-15 미션 완료 + Kafka 발행 | ☐ |
| **Kafka** | TC-16 notification-service 수신 | ☐ |
| **Mission** | TC-17 미션 이력 조회 | ☐ |
| **Redis** | TC-18 랭킹 업데이트 확인 | ☐ |
| **Feign** | TC-19 내부 API 직접 호출 | ☐ |
| **Feign + Lock** | TC-20 리워드 교환 성공 | ☐ |
| **비즈니스** | TC-21 포인트 부족 400 | ☐ |
| **비즈니스** | TC-22 품절 409 | ☐ |
| **CircuitBreaker** | TC-23 challenge-service 다운 → 503 | ☐ |
| **분산 락** | TC-24 동시 요청 재고 정합성 | ☐ |
| **QueryDSL** | TC-25 참여자 요약 (Projection) | ☐ |
| **QueryDSL** | TC-26 사용자별 참여 목록 | ☐ |
| **내부 API** | TC-27 포인트 합산 API | ☐ |

---

## 주요 서비스별 확인 포트

| 서비스 | 포트 | 역할 |
|--------|------|------|
| rewardcraft-api | 8080 | User, Reward (JWT 인증 필요) |
| challenge-service | 8082 | Challenge, Participation, Mission (인증 없음) |
| notification-service | 8081 | 콘솔 로그만 확인 (외부 호출 없음) |
| PostgreSQL | 5433 | `docker exec -it rc-postgres psql -U rc_user -d rewardcraft` |
| Redis | 6379 | `docker exec -it rc-redis redis-cli` |
