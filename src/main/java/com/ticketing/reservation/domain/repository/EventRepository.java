package com.ticketing.reservation.domain.repository;

import com.ticketing.reservation.domain.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStatus(String status);

    @Query(value = """
            SELECT e.id          AS id,
                   e.title       AS title,
                   e.description AS description,
                   e.event_date  AS eventDate,
                   e.booking_start_at AS bookingStartAt,
                   e.booking_end_at   AS bookingEndAt,
                   e.status      AS status,
                   v.name        AS venueName,
                   MIN(es.price) AS minPrice,
                   MAX(es.price) AS maxPrice
            FROM events e
                     JOIN venues v ON e.venue_id = v.id
                     LEFT JOIN event_seats es ON e.id = es.event_id
            WHERE e.status = :status
            GROUP BY e.id, e.title, e.description, e.event_date,
                     e.booking_start_at, e.booking_end_at, e.status, v.name
            """, nativeQuery = true)
    List<EventSummary> findEventSummariesByStatus(@Param("status") String status);

    interface EventSummary {
        Long getId();
        String getTitle();
        String getDescription();
        LocalDateTime getEventDate();
        LocalDateTime getBookingStartAt();
        LocalDateTime getBookingEndAt();
        String getStatus();
        String getVenueName();
        Integer getMinPrice();
        Integer getMaxPrice();
    }
}
