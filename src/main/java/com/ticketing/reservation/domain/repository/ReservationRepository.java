package com.ticketing.reservation.domain.repository;

import com.ticketing.reservation.domain.entity.Reservation;
import com.ticketing.reservation.domain.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByReservationNo(String reservationNo);

    List<Reservation> findByUserId(Long userId);

    // 만료 처리 배치용 — PENDING 상태이면서 expiresAt 이 지난 건
    @Query("SELECT r FROM Reservation r WHERE r.status = :status AND r.expiresAt < :now")
    List<Reservation> findExpiredReservations(
            @Param("status") ReservationStatus status,
            @Param("now") LocalDateTime now
    );
}
