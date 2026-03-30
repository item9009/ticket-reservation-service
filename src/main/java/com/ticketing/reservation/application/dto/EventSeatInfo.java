package com.ticketing.reservation.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventSeatInfo {

    private Long eventSeatId;
    private int price;
    private String status;  // AVAILABLE / RESERVED / SOLD

    public boolean isAvailable() {
        return "AVAILABLE".equals(this.status);
    }
}
