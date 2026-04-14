package com.ticketing.reservation.infrastructure.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * 애플리케이션 시작 시 필요한 Kafka 토픽을 자동 생성한다.
 * 토픽이 이미 존재하면 무시된다.
 */
@Configuration
public class KafkaTopicConfig {

    // 기존 토픽
    @Bean
    public NewTopic reservationTopic() {
        return TopicBuilder.name("ticket.reservation").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic paymentTopic() {
        return TopicBuilder.name("ticket.payment").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return TopicBuilder.name("ticket.payment.failed").partitions(3).replicas(1).build();
    }

    // Event Service 동기화 토픽 (EventSyncConsumer가 구독)
    @Bean
    public NewTopic eventCreatedTopic() {
        return TopicBuilder.name("event.created").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic seatStatusChangedTopic() {
        return TopicBuilder.name("event.seat.status-changed").partitions(3).replicas(1).build();
    }
}
