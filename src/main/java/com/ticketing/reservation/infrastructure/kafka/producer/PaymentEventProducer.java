package com.ticketing.reservation.infrastructure.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.reservation.infrastructure.kafka.dto.PaymentCompletedEvent;
import com.ticketing.reservation.infrastructure.kafka.dto.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private static final String TOPIC_PAYMENT_COMPLETED = "ticket.payment";
    private static final String TOPIC_PAYMENT_FAILED = "ticket.payment.failed";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendPaymentCompleted(PaymentCompletedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC_PAYMENT_COMPLETED,
                    event.getReservationId().toString(), payload);
            log.info("결제 완료 이벤트 발행: reservationId={}", event.getReservationId());
        } catch (JsonProcessingException e) {
            log.error("결제 완료 이벤트 직렬화 실패", e);
        }
    }

    public void sendPaymentFailed(PaymentFailedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC_PAYMENT_FAILED,
                    event.getReservationId().toString(), payload);
            log.info("결제 실패 이벤트 발행: reservationId={}", event.getReservationId());
        } catch (JsonProcessingException e) {
            log.error("결제 실패 이벤트 직렬화 실패", e);
        }
    }
}
