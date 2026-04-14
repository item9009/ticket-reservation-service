package com.ticketing.reservation.application.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class EventUpdateRequest {

    private String title;
    private String description;
    private LocalDateTime eventDate;
    private LocalDateTime bookingStartAt;
    private LocalDateTime bookingEndAt;
    private String status;
}
