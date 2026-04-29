package com.ticketing.reservation.application.dto;

import com.ticketing.reservation.domain.entity.Reservation;
import com.ticketing.reservation.domain.entity.ReservationSeat;
import com.ticketing.reservation.domain.entity.ReservationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

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
    private List<SeatInfo> seats;

    @Getter
    @Builder
    public static class SeatInfo {
        private Long eventSeatId;
        private int price;

        public static SeatInfo from(ReservationSeat seat) {
            return SeatInfo.builder()
                    .eventSeatId(seat.getEventSeatId())
                    .price(seat.getPrice())
                    .build();
        }
    }

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
                .seats(reservation.getSeats().stream().map(SeatInfo::from).toList())
                .build();
    }
}
