package com.ticketing.reservation.domain.entity;

public enum ReservationStatus {
    PENDING,     // 결제 대기 (10분 TTL)
    CONFIRMED,   // 결제 완료
    EXPIRED,     // TTL 초과 만료
    CANCELLED    // 사용자 취소
}
