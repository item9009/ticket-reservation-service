package com.ticketing.reservation.application.dto;

import com.ticketing.reservation.domain.entity.Event;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class EventResponse {

    private final Long id;
    private final String title;
    private final String description;
    private final LocalDateTime eventDate;
    private final LocalDateTime bookingStartAt;
    private final LocalDateTime bookingEndAt;
    private final String status;

    private EventResponse(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.description = event.getDescription();
        this.eventDate = event.getEventDate();
        this.bookingStartAt = event.getBookingStartAt();
        this.bookingEndAt = event.getBookingEndAt();
        this.status = event.getStatus();
    }

    public static EventResponse from(Event event) {
        return new EventResponse(event);
    }
}