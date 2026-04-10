package com.ticketing.reservation.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "event_seats")
@Getter
public class EventSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private Long seatId;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private String status;
}