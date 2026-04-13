# 🎫 Ticket Reservation Service

> 분산 환경에서의 대규모 티켓 예매 시스템 — Java 21 / Spring Boot / MSA

[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen)](https://spring.io/projects/spring-boot)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-7.6.0-black)](https://kafka.apache.org/)
[![Redis](https://img.shields.io/badge/Redis-7.2-red)](https://redis.io/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-K8s-blue)](https://kubernetes.io/)

---

## 📌 프로젝트 소개

콘서트 티켓 예매 시 발생하는 **수천 명의 동시 요청**을 안정적으로 처리하는 분산 시스템입니다.

Redis 분산 락으로 동시성을 제어하고, Kafka 이벤트 파이프라인으로 서비스 간 비동기 통신을 구현했습니다.
Transactional Outbox 패턴으로 데이터 정합성을 보장하며, Kubernetes로 컨테이너 오케스트레이션을 구성했습니다.

---

## 🏗️ 시스템 아키텍처

```
Client
  │ HTTP REST
  ▼
API Gateway (Spring Security + JWT)
  │ gRPC (서비스 간 통신)
  ├──▶ Reservation Service (핵심)
  ├──▶ Event Service
  ├──▶ Payment Service
  └──▶ Notification Service
         │
         ▼
    Apache Kafka
    (ticket.reservation / ticket.payment / ticket.payment.failed)
         │
         ▼
    Redis Cluster (분산 락 · 좌석 캐시 · TTL)
         │
         ▼
    MySQL (DB per Service)
         │
         ▼
    Kubernetes (HPA · ConfigMap · Secret)
```

---

## 🔧 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.0 |
| Database | MySQL 8.0 + Flyway |
| Cache / Lock | Redis 7.2 + Redisson |
| Message Queue | Apache Kafka |
| RPC | gRPC + Protocol Buffers |
| Security | Spring Security + JWT (jjwt) |
| Container | Docker + Kubernetes |
| Build | Gradle |
| Documentation | Swagger (SpringDoc OpenAPI 3) |

---

## ⚡ 핵심 구현 포인트

### 1. Redis 분산 락 — 동시성 제어

```
100명이 동시에 같은 좌석 예매 시도
→ Redis 분산 락 (Redisson RLock)
→ 1명만 성공 / 99명 409 Conflict
```

AOP 기반 `@DistributedLock` 어노테이션으로 추상화했습니다.
SpEL 표현식으로 동적 락 키를 생성하며, 좌석 ID를 정렬해 데드락을 방지합니다.

```java
@DistributedLock(key = "#request.sortedSeatKey()")
public ReservationResponse reserve(ReservationRequest request) { ... }
```

### 2. Transactional Outbox 패턴 — 이벤트 정합성 보장

```
예매 저장 + Outbox 저장 (동일 트랜잭션)
→ 스케줄러가 5초마다 Kafka로 relay
→ 실패 시 1분 후 자동 재시도
```

Kafka에 직접 발행하는 대신 DB에 먼저 저장해 메시지 유실을 원천 차단합니다.

### 3. Saga 패턴 — 분산 트랜잭션

```
예매 생성 (PENDING)
  → Kafka → 결제 처리 (Mock)
    → 성공: 티켓 발급 + 예매 CONFIRMED
    → 실패: 예매 CANCELLED (보상 트랜잭션)
```

### 4. gRPC — 서비스 간 저지연 통신

Protocol Buffers로 인터페이스를 정의하고,
Reservation Service에서 Event Service로 좌석 검증 시 gRPC를 사용합니다.

### 5. JWT 인증

Spring Security + jjwt로 Stateless 인증을 구현했습니다.
`JwtAuthFilter`가 모든 요청에서 토큰을 검증합니다.

---

## 📊 동시성 테스트 결과

```
시나리오: 100개 스레드가 동시에 같은 좌석 예매
환경: Java HttpClient + CountDownLatch

결과:
  ✅ 성공: 1건
  ❌ 실패: 99건 (409 Conflict)

→ Redis 분산 락으로 중복 예매 완벽 차단
```

---

## 🗄️ ERD

### 공연 도메인
```
VENUES (공연장)
  └── SECTIONS (구역)
        └── SEATS (좌석)
              └── EVENT_SEATS (공연별 좌석 + 가격)
                    └── EVENTS (공연)
```

### 예매 도메인
```
USERS
  └── RESERVATIONS (예매, 10분 TTL)
        ├── RESERVATION_SEATS (예매 좌석)
        ├── PAYMENTS (결제)
        ├── TICKETS (발급 티켓)
        └── OUTBOX_EVENTS (Kafka 발행 대기)
```

---

## 🚀 실행 방법

### 1. 인프라 실행 (Docker Compose)

```bash
docker compose up -d
```

MySQL + Redis + Kafka가 한 번에 실행됩니다.

### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

Flyway가 자동으로 테이블을 생성합니다 (V1~V5).

### 3. Swagger UI

```
http://localhost:8080/swagger-ui/index.html
```

### 4. Kubernetes 배포

```bash
# 이미지 빌드
./gradlew bootJar
docker build -t reservation-service:latest .

# K8s 배포
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/mysql/
kubectl apply -f k8s/redis/
kubectl apply -f k8s/kafka/
kubectl apply -f k8s/reservation-service/
```

---

## 📡 API 명세

### 인증 API

| Method | URL | 설명 |
|---|---|---|
| POST | `/api/v1/auth/signup` | 회원가입 |
| POST | `/api/v1/auth/login` | 로그인 (JWT 발급) |

### 예매 API

| Method | URL | 설명 |
|---|---|---|
| POST | `/api/v1/reservations` | 좌석 예매 |
| GET | `/api/v1/reservations/{reservationNo}` | 예매 단건 조회 |
| GET | `/api/v1/reservations/my/{userId}` | 내 예매 목록 |

### 예매 흐름

```
POST /api/v1/auth/login          → JWT 토큰 발급
POST /api/v1/reservations        → 좌석 예매 (Bearer Token 필요)
  → Redis 분산 락 획득
  → gRPC로 좌석 검증
  → DB 저장 + Outbox 저장
  → 락 해제
  → Kafka 발행 → 결제 처리 → 티켓 발급
GET /api/v1/reservations/{no}    → 예매 조회 (status: CONFIRMED)
```

---

## 📁 프로젝트 구조

```
src/main/java/com/ticketing/reservation/
├── domain/
│   ├── entity/          # JPA Entity (Reservation, Ticket, Payment ...)
│   ├── repository/      # Spring Data JPA Repository
│   └── service/         # 도메인 서비스
├── application/
│   ├── ReservationFacade.java   # 애플리케이션 진입점
│   ├── AuthService.java
│   └── dto/
├── infrastructure/
│   ├── lock/            # Redis 분산 락 (AOP)
│   ├── jwt/             # JWT 인증 필터
│   ├── grpc/            # gRPC Server / Client
│   ├── kafka/           # Kafka Producer / Consumer
│   └── outbox/          # Outbox 스케줄러
└── interfaces/
    └── api/             # REST Controller
```

---

## 🔑 환경 변수

| 변수 | 설명 | 기본값 |
|---|---|---|
| `DB_USERNAME` | MySQL 사용자 | ticketing |
| `DB_PASSWORD` | MySQL 비밀번호 | ticketing |
| `REDIS_HOST` | Redis 호스트 | localhost |
| `KAFKA_SERVERS` | Kafka 브로커 주소 | localhost:9092 |
| `JWT_SECRET` | JWT 서명 키 | (필수) |

---

## 📝 개발 단계

- [x] Stage 1 — DB 설계 & Flyway 마이그레이션
- [x] Stage 2 — Redis 분산 락 + 동시성 테스트
- [x] Stage 3 — Kafka 이벤트 파이프라인 + Saga 패턴
- [x] Stage 4 — gRPC 서비스 연동
- [x] Stage 5 — API Gateway + JWT 인증
- [x] Stage 6 — Kubernetes 배포
- [ ] Stage 7 — React 프론트엔드