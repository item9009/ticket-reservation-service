package com.ticketing.reservation.application.dto;

import com.ticketing.reservation.domain.entity.Reservation;
import com.ticketing.reservation.domain.entity.ReservationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReservationResponse {

    private Long id;
    private String reservationNo;
    private Long userId;
    private Long eventId;
    private ReservationStatus status;
    private int totalAmount;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    public static ReservationResponse from(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .reservationNo(reservation.getReservationNo())
                .userId(reservation.getUserId())
                .eventId(reservation.getEventId())
                .status(reservation.getStatus())
                .totalAmount(reservation.getTotalAmount())
                .expiresAt(reservation.getExpiresAt())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}
