package com.ticketing.reservation.application.dto;

import com.ticketing.reservation.domain.entity.EventSeat;
import lombok.Getter;

@Getter
public class EventSeatResponse {

    private final Long eventSeatId;   // 예매 요청 시 eventSeatIds에 넣는 값
    private final Long seatId;
    private final String grade;
    private final int price;
    private final String status;

    private EventSeatResponse(EventSeat eventSeat) {
        this.eventSeatId = eventSeat.getId();
        this.seatId = eventSeat.getSeatId();
        this.grade = eventSeat.getGrade();
        this.price = eventSeat.getPrice();
        this.status = eventSeat.getStatus();
    }

    public static EventSeatResponse from(EventSeat eventSeat) {
        return new EventSeatResponse(eventSeat);
    }
}