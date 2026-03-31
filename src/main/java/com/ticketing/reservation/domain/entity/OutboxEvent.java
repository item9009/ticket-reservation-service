package com.ticketing.reservation.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String aggregateType;   // "RESERVATION"

    @Column(nullable = false)
    private Long aggregateId;

    @Column(nullable = false, length = 100)
    private String eventType;       // "RESERVATION_CREATED"

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;         // JSON

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxStatus status = OutboxStatus.PENDING;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    public static OutboxEvent of(String aggregateType, Long aggregateId,
                                 String eventType, String payload) {
        OutboxEvent e = new OutboxEvent();
        e.aggregateType = aggregateType;
        e.aggregateId = aggregateId;
        e.eventType = eventType;
        e.payload = payload;
        e.status = OutboxStatus.PENDING;
        return e;
    }

    public void markProcessed() {
        this.status = OutboxStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = OutboxStatus.FAILED;
    }

    public void markPending() {
        this.status = OutboxStatus.PENDING;
        this.processedAt = null;
    }
}
