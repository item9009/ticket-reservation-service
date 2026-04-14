package com.ticketing.reservation.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event {

    @Id
    private Long id;

    @Column(nullable = false)
    private Long venueId;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    @Column(nullable = false)
    private LocalDateTime bookingStartAt;

    @Column(nullable = false)
    private LocalDateTime bookingEndAt;

    @Column(nullable = false)
    private String status;

    /**
     * Event Service의 Kafka 이벤트로 읽기 모델을 생성할 때 사용.
     * id는 Event Service가 발행한 eventId를 그대로 사용한다.
     */
    public static Event create(Long id, Long venueId, String title, String description,
                               LocalDateTime eventDate, LocalDateTime bookingStartAt,
                               LocalDateTime bookingEndAt, String status) {
        Event event = new Event();
        event.id = id;
        event.venueId = venueId;
        event.title = title;
        event.description = description;
        event.eventDate = eventDate;
        event.bookingStartAt = bookingStartAt;
        event.bookingEndAt = bookingEndAt;
        event.status = status;
        return event;
    }

    public void update(String title, String description, LocalDateTime eventDate,
                       LocalDateTime bookingStartAt, LocalDateTime bookingEndAt, String status) {
        this.title = title;
        this.description = description;
        this.eventDate = eventDate;
        this.bookingStartAt = bookingStartAt;
        this.bookingEndAt = bookingEndAt;
        this.status = status;
    }
}