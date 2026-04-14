package com.ticketing.reservation.domain.repository;

import com.ticketing.reservation.domain.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStatus(String status);
}
