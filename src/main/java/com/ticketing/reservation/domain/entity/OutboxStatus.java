package com.ticketing.reservation.domain.entity;

public enum OutboxStatus {
    PENDING,    // Kafka 발행 대기
    PROCESSED,  // 발행 완료
    FAILED      // 발행 실패 (재시도 대상)
}
