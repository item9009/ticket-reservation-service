package com.ticketing.reservation.infrastructure.outbox;

import com.ticketing.reservation.application.ReservationFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationExpiryScheduler {

    private final ReservationFacade reservationFacade;

    // 1분마다 만료된 예매 처리
    @Scheduled(fixedDelay = 60000)
    public void expireOverdueReservations() {
        log.debug("만료 예매 처리 스케줄러 실행");
        reservationFacade.expireOverdueReservations();
    }
}
