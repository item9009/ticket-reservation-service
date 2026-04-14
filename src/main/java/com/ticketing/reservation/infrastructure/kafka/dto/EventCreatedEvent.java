package com.ticketing.reservation.infrastructure.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Event Service가 이벤트 생성 시 "event.created" 토픽으로 발행하는 이벤트.
 * Reservation Service는 이를 소비해서 로컬 읽기 모델(events, event_seats 테이블)을 동기화한다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventCreatedEvent {

    private Long eventId;
    private Long venueId;
    private String title;
    private String description;
    private LocalDateTime eventDate;
    private LocalDateTime bookingStartAt;
    private LocalDateTime bookingEndAt;
    private String status;
    private List<SeatInfo> seats;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatInfo {
        private Long eventSeatId;
        private Long seatId;
        private String grade;
        private int price;
    }
}
