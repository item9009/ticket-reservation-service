package com.ticketing.reservation.application.dto;

import com.ticketing.reservation.domain.entity.Event;
import com.ticketing.reservation.domain.repository.EventRepository.EventSummary;
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
    private final String venueName;
    private final Integer minPrice;
    private final Integer maxPrice;

    private EventResponse(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.description = event.getDescription();
        this.eventDate = event.getEventDate();
        this.bookingStartAt = event.getBookingStartAt();
        this.bookingEndAt = event.getBookingEndAt();
        this.status = event.getStatus();
        this.venueName = null;
        this.minPrice = null;
        this.maxPrice = null;
    }

    private EventResponse(EventSummary summary) {
        this.id = summary.getId();
        this.title = summary.getTitle();
        this.description = summary.getDescription();
        this.eventDate = summary.getEventDate();
        this.bookingStartAt = summary.getBookingStartAt();
        this.bookingEndAt = summary.getBookingEndAt();
        this.status = summary.getStatus();
        this.venueName = summary.getVenueName();
        this.minPrice = summary.getMinPrice();
        this.maxPrice = summary.getMaxPrice();
    }

    public static EventResponse from(Event event) {
        return new EventResponse(event);
    }

    public static EventResponse from(EventSummary summary) {
        return new EventResponse(summary);
    }
}