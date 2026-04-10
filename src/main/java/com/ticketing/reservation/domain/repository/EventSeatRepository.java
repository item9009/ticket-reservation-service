package com.ticketing.reservation.domain.repository;

import com.ticketing.reservation.domain.entity.EventSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventSeatRepository extends JpaRepository<EventSeat, Long> {

    List<EventSeat> findByEventIdAndIdIn(Long eventId, List<Long> ids);
}
