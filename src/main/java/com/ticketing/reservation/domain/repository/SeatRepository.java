package com.ticketing.reservation.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<com.ticketing.reservation.domain.entity.Seat, Long> {

    @Query(value = """
            SELECT s.id AS seatId, sec.grade AS grade
            FROM seats s
                     JOIN sections sec ON s.section_id = sec.id
            WHERE sec.venue_id = :venueId
            """, nativeQuery = true)
    List<SeatGradeProjection> findSeatGradesByVenueId(@Param("venueId") Long venueId);

    interface SeatGradeProjection {
        Long getSeatId();
        String getGrade();
    }
}
