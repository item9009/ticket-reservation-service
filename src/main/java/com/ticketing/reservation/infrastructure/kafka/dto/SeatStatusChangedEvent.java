package com.ticketing.reservation.infrastructure.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Event Service가 좌석 상태 변경 시 "event.seat.status-changed" 토픽으로 발행하는 이벤트.
 * 예: 공연 취소로 전체 좌석을 CANCELLED로 변경하거나, 관리자가 특정 좌석을 비활성화하는 경우.
 * (예매에 의한 RESERVED 상태 변경은 Reservation Service가 직접 관리한다.)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatStatusChangedEvent {

    private Long eventId;
    private Long eventSeatId;
    private String newStatus;
}
