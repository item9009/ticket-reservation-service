package com.ticketing.reservation.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.reservation.application.dto.*;
import com.ticketing.reservation.domain.entity.OutboxEvent;
import com.ticketing.reservation.domain.entity.Reservation;
import com.ticketing.reservation.domain.entity.ReservationStatus;
import com.ticketing.reservation.domain.repository.OutboxEventRepository;
import com.ticketing.reservation.domain.repository.ReservationRepository;
import com.ticketing.reservation.domain.service.ReservationDomainService;
import com.ticketing.reservation.infrastructure.grpc.EventSeatGrpcClient;
import com.ticketing.reservation.infrastructure.lock.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationFacade {

    private final ReservationDomainService reservationDomainService;
    private final ReservationRepository reservationRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final EventSeatGrpcClient eventSeatGrpcClient;
    private final ObjectMapper objectMapper;

    /**
     * 좌석 ID 목록을 정렬한 문자열을 락 키로 사용.
     * 예: eventSeatIds=[1003,1001,1002] → 락 키: "LOCK:SEAT:1001,1002,1003"
     * 정렬을 통해 서로 다른 순서로 요청해도 동일 키로 수렴 → 데드락 방지
     */
    @DistributedLock(key = "#request.sortedSeatKey()")
    public ReservationResponse reserve(ReservationRequest request) {
        // 1. gRPC로 좌석 유효성 검증 + 가격 조회
        List<EventSeatInfo> seatInfos = eventSeatGrpcClient.validateAndGetSeats(
                request.getEventId(), request.getEventSeatIds()
        );

        // 2. 도메인 서비스에서 예매 생성 (상태: PENDING)
        Reservation reservation = reservationDomainService.createReservation(
                request.getUserId(), request.getEventId(), seatInfos
        );

        // 3. Outbox 이벤트 저장 — 예매 저장과 동일 트랜잭션 (AopForTransaction의 REQUIRES_NEW)
        OutboxEvent outboxEvent = OutboxEvent.of(
                "RESERVATION",
                reservation.getId(),
                "RESERVATION_CREATED",
                toJson(ReservationCreatedEvent.from(reservation))
        );
        outboxEventRepository.save(outboxEvent);

        log.info("예매 생성 완료: reservationNo={}, userId={}, seats={}",
                reservation.getReservationNo(),
                request.getUserId(),
                request.getEventSeatIds()
        );

        return ReservationResponse.from(reservation);
    }

    public ReservationResponse getReservation(String reservationNo) {
        Reservation reservation = reservationRepository.findByReservationNo(reservationNo)
                .orElseThrow(() -> new IllegalArgumentException(
                        "예매를 찾을 수 없습니다: " + reservationNo
                ));
        return ReservationResponse.from(reservation);
    }

    public List<ReservationResponse> getMyReservations(Long userId) {
        return reservationRepository.findByUserId(userId).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    // 만료 배치 — @Scheduled에서 호출
    public void expireOverdueReservations() {
        List<Reservation> expired = reservationRepository.findExpiredReservations(
                ReservationStatus.PENDING, LocalDateTime.now()
        );
        expired.forEach(r -> {
            r.expire();
            log.info("예매 만료 처리: reservationNo={}", r.getReservationNo());
        });
        reservationRepository.saveAll(expired);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 직렬화 실패", e);
        }
    }
}
