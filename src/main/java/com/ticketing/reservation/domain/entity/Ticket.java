package com.ticketing.reservation.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tickets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reservationId;

    @Column(nullable = false)
    private Long eventSeatId;

    @Column(unique = true, nullable = false, length = 50)
    private String ticketCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;

    @CreationTimestamp
    private LocalDateTime issuedAt;

    public static Ticket create(Long reservationId, Long eventSeatId) {
        Ticket t = new Ticket();
        t.reservationId = reservationId;
        t.eventSeatId = eventSeatId;
        t.ticketCode = "TKT_" + UUID.randomUUID().toString().substring(0, 16).toUpperCase();
        t.status = TicketStatus.VALID;
        return t;
    }
}
