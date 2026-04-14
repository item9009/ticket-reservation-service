package com.ticketing.reservation.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "event_seats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventSeat {

    @Id
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private Long seatId;

    @Column(nullable = false, length = 10)
    private String grade;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private String status;

    /**
     * Event Service의 Kafka 이벤트로 읽기 모델을 생성할 때 사용.
     * id는 Event Service가 발행한 eventSeatId를 그대로 사용한다.
     */
    public static EventSeat create(Long id, Long eventId, Long seatId, String grade, int price, String status) {
        EventSeat seat = new EventSeat();
        seat.id = id;
        seat.eventId = eventId;
        seat.seatId = seatId;
        seat.grade = grade;
        seat.price = price;
        seat.status = status;
        return seat;
    }

    public void updateStatus(String newStatus) {
        this.status = newStatus;
    }
}
