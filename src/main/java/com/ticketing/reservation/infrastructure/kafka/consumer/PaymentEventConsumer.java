package com.ticketing.reservation.infrastructure.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.reservation.domain.entity.EventSeat;
import com.ticketing.reservation.domain.entity.Reservation;
import com.ticketing.reservation.domain.entity.Ticket;
import com.ticketing.reservation.domain.repository.EventSeatRepository;
import com.ticketing.reservation.domain.repository.ReservationRepository;
import com.ticketing.reservation.domain.repository.TicketRepository;

import java.util.List;
import com.ticketing.reservation.infrastructure.kafka.dto.PaymentCompletedEvent;
import com.ticketing.reservation.infrastructure.kafka.dto.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final ReservationRepository reservationRepository;
    private final EventSeatRepository eventSeatRepository;
    private final TicketRepository ticketRepository;
    private final ObjectMapper objectMapper;

    // 결제 완료 → 티켓 발급 + 예매 CONFIRMED
    @KafkaListener(
            topics = "ticket.payment",
            groupId = "ticket-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumePaymentCompleted(String message) {
        try {
            PaymentCompletedEvent event = objectMapper.readValue(
                    message, PaymentCompletedEvent.class
            );
            log.info("결제 완료 이벤트 수신: reservationId={}", event.getReservationId());

            // 예매 상태 CONFIRMED 변경
            Reservation reservation = reservationRepository
                    .findById(event.getReservationId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "예매를 찾을 수 없습니다: " + event.getReservationId()
                    ));
            reservation.confirm();

            // event_seats 상태 SOLD로 업데이트
            List<Long> eventSeatIds = reservation.getSeats().stream()
                    .map(seat -> seat.getEventSeatId())
                    .toList();
            List<EventSeat> eventSeats = eventSeatRepository.findAllById(eventSeatIds);
            eventSeats.forEach(es -> es.updateStatus("SOLD"));
            eventSeatRepository.saveAll(eventSeats);

            // 티켓 발급
            reservation.getSeats().forEach(seat -> {
                Ticket ticket = Ticket.create(
                        reservation.getId(),
                        seat.getEventSeatId()
                );
                ticketRepository.save(ticket);
                log.info("티켓 발급 완료: ticketCode={}", ticket.getTicketCode());
            });

        } catch (Exception e) {
            log.error("결제 완료 이벤트 처리 실패: {}", e.getMessage(), e);
        }
    }

    // 결제 실패 → 예매 CANCELLED (보상 트랜잭션)
    @KafkaListener(
            topics = "ticket.payment.failed",
            groupId = "ticket-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumePaymentFailed(String message) {
        try {
            PaymentFailedEvent event = objectMapper.readValue(
                    message, PaymentFailedEvent.class
            );
            log.warn("결제 실패 이벤트 수신: reservationId={}, reason={}",
                    event.getReservationId(), event.getReason());

            // 예매 취소 (보상 트랜잭션)
            Reservation reservation = reservationRepository
                    .findById(event.getReservationId())
                    .orElseThrow();
            reservation.cancel();

            // event_seats 상태 AVAILABLE로 복구
            List<Long> eventSeatIds = reservation.getSeats().stream()
                    .map(seat -> seat.getEventSeatId())
                    .toList();
            List<EventSeat> eventSeats = eventSeatRepository.findAllById(eventSeatIds);
            eventSeats.forEach(es -> es.updateStatus("AVAILABLE"));
            eventSeatRepository.saveAll(eventSeats);

        } catch (Exception e) {
            log.error("결제 실패 이벤트 처리 실패: {}", e.getMessage(), e);
        }
    }
}
