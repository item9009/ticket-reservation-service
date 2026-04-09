package com.ticketing.reservation.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reservationId;

    @Column(unique = true, nullable = false, length = 100)
    private String paymentKey;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(length = 30)
    private String method;

    private LocalDateTime paidAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public static Payment create(Long reservationId, String paymentKey,
                                 int amount, String method) {
        Payment p = new Payment();
        p.reservationId = reservationId;
        p.paymentKey = paymentKey;
        p.amount = amount;
        p.status = PaymentStatus.COMPLETED;
        p.method = method;
        p.paidAt = LocalDateTime.now();
        return p;
    }
}
