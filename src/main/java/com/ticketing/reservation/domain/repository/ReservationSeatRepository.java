package com.ticketing.reservation.domain.repository;

import com.ticketing.reservation.domain.entity.ReservationSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, Long> {
    // 이미 예매된 좌석인지 확인
    boolean existsByEventSeatIdIn(List<Long> eventSeatIds);
}
