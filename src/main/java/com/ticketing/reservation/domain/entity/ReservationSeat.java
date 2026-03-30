package com.ticketing.reservation.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reservation_seats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(nullable = false)
    private Long eventSeatId;

    @Column(nullable = false)
    private int price;

    public static ReservationSeat of(Reservation reservation, Long eventSeatId, int price) {
        ReservationSeat rs = new ReservationSeat();
        rs.reservation = reservation;
        rs.eventSeatId = eventSeatId;
        rs.price = price;
        return rs;
    }
}
