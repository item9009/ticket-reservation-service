package com.ticketing.reservation.infrastructure.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent {
    private Long reservationId;
    private String reservationNo;
    private Long userId;
    private String paymentKey;
    private int amount;
    private String method;
    private LocalDateTime paidAt;
}