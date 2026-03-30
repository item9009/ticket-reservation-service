package com.ticketing.reservation.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String reservationNo;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(nullable = false)
    private int totalAmount;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationSeat> seats = new ArrayList<>();

    // 정적 팩토리 메서드 — new 대신 이걸로만 생성
    public static Reservation create(Long userId, Long eventId, int totalAmount) {
        Reservation r = new Reservation();
        r.reservationNo = generateReservationNo();
        r.userId = userId;
        r.eventId = eventId;
        r.status = ReservationStatus.PENDING;
        r.totalAmount = totalAmount;
        r.expiresAt = LocalDateTime.now().plusMinutes(10);
        return r;
    }

    public void addSeat(ReservationSeat seat) {
        this.seats.add(seat);
    }

    public void confirm() {
        validateStatus(ReservationStatus.PENDING);
        this.status = ReservationStatus.CONFIRMED;
    }

    public void expire() {
        if (this.status == ReservationStatus.CONFIRMED) return;
        this.status = ReservationStatus.EXPIRED;
    }

    public void cancel() {
        validateStatus(ReservationStatus.CONFIRMED);
        this.status = ReservationStatus.CANCELLED;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    private void validateStatus(ReservationStatus expected) {
        if (this.status != expected) {
            throw new IllegalStateException(
                    "잘못된 상태 전이: " + this.status + " -> " + expected
            );
        }
    }

    private static String generateReservationNo() {
        return "R" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
               + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
