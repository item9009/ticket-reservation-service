package com.ticketing.reservation.application.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
public class EventCreateRequest {

    private Long venueId;
    private String title;
    private String description;
    private LocalDateTime eventDate;
    private LocalDateTime bookingStartAt;
    private LocalDateTime bookingEndAt;

    /**
     * grade별 가격 설정. 예: {"VIP": 150000, "R": 100000, "S": 70000}
     * 미설정 grade는 기본값 0원으로 처리됩니다.
     */
    private Map<String, Integer> gradePrices;
}
