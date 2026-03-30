package com.ticketing.reservation.application.dto;

import com.ticketing.reservation.domain.entity.Reservation;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ReservationCreatedEvent {

    private Long reservationId;
    private String reservationNo;
    private Long userId;
    private Long eventId;
    private int totalAmount;
    private LocalDateTime expiresAt;
    private List<Long> eventSeatIds;

    public static ReservationCreatedEvent from(Reservation reservation) {
        return ReservationCreatedEvent.builder()
                .reservationId(reservation.getId())
                .reservationNo(reservation.getReservationNo())
                .userId(reservation.getUserId())
                .eventId(reservation.getEventId())
                .totalAmount(reservation.getTotalAmount())
                .expiresAt(reservation.getExpiresAt())
                .eventSeatIds(
                        reservation.getSeats().stream()
                                .map(s -> s.getEventSeatId())
                                .collect(Collectors.toList())
                )
                .build();
    }
}
