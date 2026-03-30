package com.ticketing.reservation.domain.service;

import com.ticketing.reservation.application.dto.EventSeatInfo;
import com.ticketing.reservation.domain.entity.Reservation;
import com.ticketing.reservation.domain.entity.ReservationSeat;
import com.ticketing.reservation.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationDomainService {

    private final ReservationRepository reservationRepository;

    public Reservation createReservation(Long userId, Long eventId,
                                          List<EventSeatInfo> seatInfos) {
        // 1. 좌석 상태 검증
        seatInfos.forEach(seat -> {
            if (!seat.isAvailable()) {
                throw new IllegalStateException(
                        "이미 선점된 좌석입니다: eventSeatId=" + seat.getEventSeatId()
                );
            }
        });

        // 2. 총 금액 계산
        int totalAmount = seatInfos.stream()
                .mapToInt(EventSeatInfo::getPrice)
                .sum();

        // 3. 예매 생성
        Reservation reservation = Reservation.create(userId, eventId, totalAmount);

        // 4. 예매 좌석 추가
        seatInfos.forEach(seat -> {
            ReservationSeat reservationSeat = ReservationSeat.of(
                    reservation, seat.getEventSeatId(), seat.getPrice()
            );
            reservation.addSeat(reservationSeat);
        });

        return reservationRepository.save(reservation);
    }
}
