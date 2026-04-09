package com.ticketing.reservation.infrastructure.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.reservation.application.dto.ReservationCreatedEvent;
import com.ticketing.reservation.infrastructure.kafka.dto.PaymentCompletedEvent;
import com.ticketing.reservation.infrastructure.kafka.dto.PaymentFailedEvent;
import com.ticketing.reservation.infrastructure.kafka.producer.PaymentEventProducer;
import com.ticketing.reservation.domain.entity.Payment;
import com.ticketing.reservation.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventConsumer {

    private final PaymentEventProducer paymentEventProducer;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "ticket.reservation",
            groupId = "payment-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(String message) {
        try {
            ReservationCreatedEvent event = objectMapper.readValue(
                    message, ReservationCreatedEvent.class
            );
            log.info("예매 이벤트 수신: reservationId={}", event.getReservationId());

            // Mock 결제 처리 (실제 PG 연동 대신)
            processPayment(event);

        } catch (Exception e) {
            log.error("예매 이벤트 처리 실패: {}", e.getMessage(), e);
        }
    }

    private void processPayment(ReservationCreatedEvent event) {
        try {
            // Mock 결제 성공 처리
            String paymentKey = "PAY_" + UUID.randomUUID().toString().substring(0, 16).toUpperCase();

            // payments 테이블 저장
            Payment payment = Payment.create(
                    event.getReservationId(),
                    paymentKey,
                    event.getTotalAmount(),
                    "CARD"
            );
            paymentRepository.save(payment);

            // 결제 완료 이벤트 발행
            PaymentCompletedEvent completedEvent = PaymentCompletedEvent.builder()
                    .reservationId(event.getReservationId())
                    .reservationNo(event.getReservationNo())
                    .userId(event.getUserId())
                    .paymentKey(paymentKey)
                    .amount(event.getTotalAmount())
                    .method("CARD")
                    .paidAt(LocalDateTime.now())
                    .build();

            paymentEventProducer.sendPaymentCompleted(completedEvent);
            log.info("Mock 결제 완료: reservationId={}, paymentKey={}",
                    event.getReservationId(), paymentKey);

        } catch (Exception e) {
            log.error("결제 처리 실패: reservationId={}", event.getReservationId(), e);

            // 결제 실패 이벤트 발행 (보상 트랜잭션)
            PaymentFailedEvent failedEvent = PaymentFailedEvent.builder()
                    .reservationId(event.getReservationId())
                    .reservationNo(event.getReservationNo())
                    .userId(event.getUserId())
                    .reason(e.getMessage())
                    .build();

            paymentEventProducer.sendPaymentFailed(failedEvent);
        }
    }
}
