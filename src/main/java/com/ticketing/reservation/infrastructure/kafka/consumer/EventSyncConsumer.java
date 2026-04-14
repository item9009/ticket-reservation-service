package com.ticketing.reservation.infrastructure.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.reservation.domain.entity.Event;
import com.ticketing.reservation.domain.entity.EventSeat;
import com.ticketing.reservation.domain.repository.EventRepository;
import com.ticketing.reservation.domain.repository.EventSeatRepository;
import com.ticketing.reservation.infrastructure.kafka.dto.EventCreatedEvent;
import com.ticketing.reservation.infrastructure.kafka.dto.SeatStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event Service가 발행하는 이벤트를 소비해서 로컬 읽기 모델을 동기화한다.
 *
 * <p>CQRS 읽기 모델 패턴:
 * <ul>
 *   <li>Event Service(원본) → Kafka → Reservation Service(읽기 복사본)
 *   <li>조회 시 네트워크 호출 없이 로컬 DB 직접 사용
 *   <li>현재 로컬 개발에서는 Flyway 시드 데이터로 동작하며, Event Service 분리 시 이 컨슈머가 데이터를 채운다
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventSyncConsumer {

    private final EventRepository eventRepository;
    private final EventSeatRepository eventSeatRepository;
    private final ObjectMapper objectMapper;

    /**
     * 이벤트 생성 시 events + event_seats 읽기 모델 동기화.
     * 이미 존재하면 업데이트, 없으면 삽입 (upsert).
     */
    @KafkaListener(
            topics = "event.created",
            groupId = "reservation-event-sync",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleEventCreated(String message) {
        try {
            EventCreatedEvent event = objectMapper.readValue(message, EventCreatedEvent.class);
            log.info("이벤트 생성 동기화: eventId={}, title={}", event.getEventId(), event.getTitle());

            syncEvent(event);
            syncEventSeats(event);

        } catch (Exception e) {
            log.error("이벤트 생성 동기화 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 좌석 상태 변경 시 event_seats 읽기 모델 동기화.
     * 예: 공연 취소(CANCELLED), 관리자 비활성화 등 Event Service 주도의 상태 변경.
     * (예매에 의한 RESERVED 변경은 Reservation Service가 직접 처리한다.)
     */
    @KafkaListener(
            topics = "event.seat.status-changed",
            groupId = "reservation-event-sync",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleSeatStatusChanged(String message) {
        try {
            SeatStatusChangedEvent event = objectMapper.readValue(message, SeatStatusChangedEvent.class);
            log.info("좌석 상태 변경 동기화: eventSeatId={}, newStatus={}",
                    event.getEventSeatId(), event.getNewStatus());

            eventSeatRepository.findById(event.getEventSeatId())
                    .ifPresentOrElse(
                            seat -> {
                                seat.updateStatus(event.getNewStatus());
                                log.info("좌석 상태 업데이트 완료: eventSeatId={}", event.getEventSeatId());
                            },
                            () -> log.warn("좌석을 찾을 수 없음: eventSeatId={}", event.getEventSeatId())
                    );

        } catch (Exception e) {
            log.error("좌석 상태 동기화 실패: {}", e.getMessage(), e);
        }
    }

    private void syncEvent(EventCreatedEvent payload) {
        eventRepository.findById(payload.getEventId())
                .ifPresentOrElse(
                        existing -> {
                            existing.update(
                                    payload.getTitle(),
                                    payload.getDescription(),
                                    payload.getEventDate(),
                                    payload.getBookingStartAt(),
                                    payload.getBookingEndAt(),
                                    payload.getStatus()
                            );
                            log.debug("이벤트 업데이트: eventId={}", payload.getEventId());
                        },
                        () -> {
                            Event event = Event.create(
                                    payload.getEventId(),
                                    payload.getVenueId(),
                                    payload.getTitle(),
                                    payload.getDescription(),
                                    payload.getEventDate(),
                                    payload.getBookingStartAt(),
                                    payload.getBookingEndAt(),
                                    payload.getStatus()
                            );
                            eventRepository.save(event);
                            log.debug("이벤트 삽입: eventId={}", payload.getEventId());
                        }
                );
    }

    private void syncEventSeats(EventCreatedEvent payload) {
        if (payload.getSeats() == null || payload.getSeats().isEmpty()) {
            return;
        }

        payload.getSeats().forEach(seatInfo ->
                eventSeatRepository.findById(seatInfo.getEventSeatId())
                        .ifPresentOrElse(
                                existing -> {
                                    existing.updateStatus("AVAILABLE");
                                    log.debug("좌석 업데이트: eventSeatId={}", seatInfo.getEventSeatId());
                                },
                                () -> {
                                    EventSeat seat = EventSeat.create(
                                            seatInfo.getEventSeatId(),
                                            payload.getEventId(),
                                            seatInfo.getSeatId(),
                                            seatInfo.getGrade(),
                                            seatInfo.getPrice(),
                                            "AVAILABLE"
                                    );
                                    eventSeatRepository.save(seat);
                                    log.debug("좌석 삽입: eventSeatId={}", seatInfo.getEventSeatId());
                                }
                        )
        );
    }
}