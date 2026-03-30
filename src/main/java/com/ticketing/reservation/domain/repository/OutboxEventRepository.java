package com.ticketing.reservation.domain.repository;

import com.ticketing.reservation.domain.entity.OutboxEvent;
import com.ticketing.reservation.domain.entity.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    // 폴링 배치용 — PENDING 상태 이벤트를 오래된 것부터 최대 100건 가져옴
    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
