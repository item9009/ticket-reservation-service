package com.ticketing.reservation.domain.repository;

import com.ticketing.reservation.domain.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByReservationId(Long reservationId);
}
