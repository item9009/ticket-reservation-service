# 분산 환경 티켓 예매 시스템

고동시성 환경에서의 좌석 예매 문제를 분산 시스템 기술로 해결한 포트폴리오 프로젝트입니다.  
MSA 아키텍처 기반으로 Redis 분산락, Kafka Transactional Outbox, gRPC, Kubernetes를 직접 설계하고 구현했습니다.
 
---

## 목차

- [기술 스택](#기술-스택)
- [시스템 아키텍처](#시스템-아키텍처)
- [핵심 기술 구현](#핵심-기술-구현)
    - [Redis 분산락 (AOP 기반)](#1-redis-분산락--aop-기반)
    - [Kafka Transactional Outbox 패턴](#2-kafka-transactional-outbox-패턴)
    - [Saga 패턴 (결제/티켓 플로우)](#3-saga-패턴--결제티켓-플로우)
    - [gRPC 서비스 간 통신](#4-grpc-서비스-간-통신)
    - [JWT 인증 + Spring Security](#5-jwt-인증--spring-security)
    - [Kubernetes 배포](#6-kubernetes-배포)
- [기술 선택 근거](#기술-선택-근거)
- [트러블슈팅](#트러블슈팅)
- [동시성 테스트 결과](#동시성-테스트-결과)
- [ERD](#erd)
- [실행 방법](#실행-방법)
---

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Database | MySQL 8.0, Redis |
| Messaging | Apache Kafka |
| RPC | gRPC (Protocol Buffers) |
| Auth | JWT, Spring Security |
| Infra | Docker, Kubernetes (kind), Flyway |
| Frontend | React (Vite + Tailwind CSS + Zustand) |
 
---

## 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                    Kubernetes Cluster (kind)                  │
│                                                               │
│  ┌──────────────┐    gRPC    ┌──────────────────────────┐   │
│  │   React UI   │ ─────────> │   reservation-service    │   │
│  │  (NodePort   │            │   (Spring Boot / JWT)    │   │
│  │   :30080)    │            └────────────┬─────────────┘   │
│  └──────────────┘                         │                   │
│                                    ┌──────┴──────┐           │
│                                    │             │           │
│                              ┌─────▼──┐    ┌────▼────┐      │
│                              │ Redis  │    │  MySQL  │      │
│                              │(분산락) │    │(Flyway) │      │
│                              └────────┘    └─────────┘      │
│                                    │                          │
│                            ┌───────▼──────┐                  │
│                            │    Kafka     │                  │
│                            │  (Outbox →   │                  │
│                            │   Consumer)  │                  │
│                            └─────────────┘                  │
└─────────────────────────────────────────────────────────────┘
```
 
---

## 핵심 기술 구현

### 1. Redis 분산락 — AOP 기반

**문제**: 동일 좌석에 다수의 요청이 동시에 들어올 경우 중복 예매 발생 가능.

**해결**: `@DistributedLock` 커스텀 어노테이션을 AOP로 구현, 비즈니스 로직과 락 처리를 완전히 분리.

```java
@DistributedLock(key = "#eventSeatId", waitTime = 5, leaseTime = 3)
public ReservationResponse reserve(Long eventSeatId, Long userId) {
    // 락 획득 후 비즈니스 로직 실행
}
```

- SpEL(Spring Expression Language)로 동적 키 생성 — 좌석 ID별로 독립적인 락 적용
- `REQUIRES_NEW` 전파 레벨로 락 트랜잭션과 비즈니스 트랜잭션을 분리 (`AopForTransaction`)
- 100개 스레드 동시 요청 테스트에서 **1 성공 / 99 실패(409)** 검증
### 2. Kafka Transactional Outbox 패턴

**문제**: 예매 저장과 이벤트 발행이 별개의 작업이라 둘 중 하나만 성공하는 데이터 불일치 가능성 존재.

**해결**: DB 트랜잭션 내에서 Outbox 테이블에 이벤트를 함께 저장하고, 별도 스케줄러가 발행 처리.

```
예매 요청
 └─ 트랜잭션 시작
     ├─ reservations 테이블 저장
     └─ outbox_events 테이블 저장 (PENDING)
          └─ 스케줄러(5초) → Kafka 발행 → PUBLISHED 상태 업데이트
               └─ FAILED 상태 → 1분 후 재시도
```

- 예매와 이벤트 발행의 원자성 보장
- 재시도 정책: 발행 실패 시 1분 후 자동 재처리 (5분 vs 1분 트레이드오프 검토 후 선택)
### 3. Saga 패턴 — 결제/티켓 플로우

**문제**: 결제와 티켓 발급은 각각 독립적인 작업이지만 하나가 실패하면 롤백이 필요.

**해결**: Choreography 방식의 Saga 패턴으로 각 서비스가 이벤트를 구독하고 보상 트랜잭션 처리.

```
예매 완료 이벤트 발행
 └─ Payment Consumer: 결제 처리
      ├─ 성공 → 결제 완료 이벤트 발행
      │         └─ Ticket Consumer: 티켓 발급
      └─ 실패 → 예매 취소 이벤트 발행 (보상 트랜잭션)
```

### 4. gRPC 서비스 간 통신

**선택 이유**: REST 대비 Protocol Buffers 직렬화로 성능 이점, 타입 안전한 인터페이스 계약.

- proto 파일 위치: `src/main/proto/` (빌드 시 자동 코드 생성)
- 서비스 간 직접 호출이 필요한 실시간 조회에 사용 (Kafka는 비동기 이벤트 처리에 사용)
### 5. JWT 인증 + Spring Security

- 로그인/회원가입 → JWT Access Token 발급
- 모든 예매 API에 토큰 검증 적용
- `/actuator/**` 엔드포인트 permitAll 처리 — K8s readiness/liveness probe 정상 동작
### 6. Kubernetes 배포

- kind(Kubernetes in Docker) 클러스터 로컬 환경 구성
- 구성 요소: Namespace, ConfigMap, Secret, Deployment (MySQL / Redis / Kafka / app), HPA
- NodePort 30080으로 외부 접근 (kind 노드 IP: `172.19.0.4`)
- Kafka Controller 포트 9093을 K8s Service에 별도 노출
---

## 기술 선택 근거

### Kafka vs RabbitMQ
Kafka를 선택한 이유는 메시지 영속성과 재처리 용이성 때문입니다.  
티켓 예매는 결제/발급 이력이 중요한 도메인이라 7일 보관되는 Kafka의 메시지 로그가 적합했고,  
Consumer 장애 발생 시 오프셋 기반으로 재처리가 가능한 점이 결정적이었습니다.

### Redis 분산락 vs DB 비관적 락
DB 비관적 락은 락을 획득하려는 요청이 많아질수록 DB 커넥션을 점유한 채 대기하여 커넥션 풀 고갈 위험이 있습니다.  
Redis 분산락은 별도 인프라로 DB 부하를 분산하고, TTL 기반으로 데드락을 자연스럽게 방지합니다.

### Outbox 패턴 vs 직접 Kafka 발행
트랜잭션 내 Kafka 직접 발행은 DB 저장 성공 후 Kafka 발행 실패 시 이벤트 유실이 발생합니다.  
Outbox 패턴은 이벤트를 DB와 같은 트랜잭션에 저장하므로 원자성이 보장되고, 장애 복구 후 재발행이 가능합니다.

### gRPC vs REST (서비스 간 통신)
서비스 내부 통신에서 REST는 텍스트 기반 직렬화 오버헤드가 있습니다.  
gRPC + Protocol Buffers는 바이너리 직렬화로 속도가 빠르고, proto 파일이 인터페이스 계약서 역할을 해서  
서비스 간 타입 불일치 버그를 컴파일 타임에 잡을 수 있습니다.
 
---

## 트러블슈팅

### MySQL 8.0 JDBC 연결 오류
**문제**: `allowPublicKeyRetrieval` 관련 JDBC 연결 실패  
**원인**: MySQL 8.0에서 RSA 공개키 교환 옵션 기본값 변경  
**해결**: JDBC URL에 `allowPublicKeyRetrieval=true&useSSL=false` 추가

### K8s에서 Kafka 브로커 연결 실패
**문제**: Kafka Controller가 Pod 간 통신에서 응답 없음  
**원인**: Controller 포트 9093이 K8s Service에 노출되지 않음  
**해결**: Service 스펙에 9093 포트 명시적 추가

### gRPC proto 파일 컴파일 안 됨
**문제**: 빌드 시 proto 파일을 찾지 못함  
**원인**: 파일 위치가 `src/main/resources/proto/`로 잘못 설정됨  
**해결**: `src/main/proto/`로 이동 — protobuf gradle 플러그인의 기본 소스셋 경로

### Spring Security + K8s probe 충돌
**문제**: readiness probe가 401 응답을 받아 Pod이 계속 재시작  
**원인**: `/actuator/health` 엔드포인트에 JWT 인증 적용됨  
**해결**: Security Config에서 `/actuator/**` permitAll 처리
 
---

## 동시성 테스트 결과

100개 스레드가 동일 좌석(eventSeatId: 1)에 동시에 예매 요청을 보낸 결과:

```
총 요청:  100개
성공:      1개  (200 OK)
실패:     99개  (409 Conflict - 이미 예매된 좌석)
```

Redis 분산락이 정상적으로 동작하여 중복 예매가 발생하지 않음을 확인.
 
---

## ERD

**User / Event 도메인**
```
users          events          venues
─────────      ──────────      ──────────
id (PK)        id (PK)         id (PK)
email          title           name
password       venue_id (FK)   address
name           event_date      capacity
created_at     created_at      created_at
 
event_seats
──────────────────
id (PK)
event_id (FK)
seat_number
grade
price
status (AVAILABLE / RESERVED)
```

**Reservation / Payment 도메인**
```
reservations        payments            tickets
────────────────    ────────────────    ──────────────
id (PK)             id (PK)             id (PK)
reservation_no      reservation_id (FK) reservation_id (FK)
user_id (FK)        amount              ticket_no
event_seat_id (FK)  status              issued_at
status              paid_at             created_at
total_amount
created_at
 
outbox_events
──────────────────────────
id (PK)
aggregate_type
aggregate_id
event_type
payload (JSON)
status (PENDING / PUBLISHED / FAILED)
created_at
```
 
---

## 실행 방법

### 사전 요구사항
- Docker Desktop (Kubernetes 활성화)
- kind CLI
- Java 21
- Node.js 18+
### 로컬 실행 (Docker Compose)

```bash
# 인프라 기동 (MySQL, Redis, Kafka)
docker-compose up -d
 
# 백엔드 실행
./gradlew bootRun
 
# 프론트엔드 실행
cd frontend
npm install
npm run dev
```

### Kubernetes 배포

```bash
# kind 클러스터 생성
kind create cluster --name ticketing
 
# 매니페스트 배포
kubectl apply -f k8s/
 
# 접근 확인 (kind 노드 IP)
kubectl get nodes -o wide
# → http://<NODE_IP>:30080
```
 
---

## GitHub

[item9009/ticket-reservation-service](https://github.com/item9009/ticket-reservation-service)