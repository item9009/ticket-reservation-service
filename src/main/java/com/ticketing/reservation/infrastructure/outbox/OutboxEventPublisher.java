package com.ticketing.reservation.infrastructure.outbox;

import com.ticketing.reservation.domain.entity.OutboxEvent;
import com.ticketing.reservation.domain.entity.OutboxStatus;
import com.ticketing.reservation.domain.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private static final String TOPIC_RESERVATION = "ticket.reservation";

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    // 5초마다 PENDING 이벤트를 Kafka로 relay
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents =
                outboxEventRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        if (pendingEvents.isEmpty()) return;

        log.debug("Outbox 발행 처리: {}건", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                kafkaTemplate.send(
                        TOPIC_RESERVATION,
                        event.getAggregateId().toString(),
                        event.getPayload()
                ).whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Kafka 발행 실패: outboxEventId={}", event.getId(), ex);
                        event.markFailed();
                    }
                });
                event.markProcessed();
            } catch (Exception e) {
                log.error("Outbox 처리 중 오류: outboxEventId={}", event.getId(), e);
                event.markFailed();
            }
        }
    }
}
