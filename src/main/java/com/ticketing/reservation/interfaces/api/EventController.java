package com.ticketing.reservation.interfaces.api;

import com.ticketing.reservation.application.dto.EventResponse;
import com.ticketing.reservation.application.dto.EventSeatResponse;
import com.ticketing.reservation.domain.repository.EventRepository;
import com.ticketing.reservation.domain.repository.EventSeatRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "이벤트 API", description = "이벤트 및 좌석 조회 API")
public class EventController {

    private final EventRepository eventRepository;
    private final EventSeatRepository eventSeatRepository;

    @GetMapping
    @Operation(summary = "이벤트 목록 조회", description = "예매 가능한 이벤트 목록을 반환합니다.")
    public ResponseEntity<List<EventResponse>> getEvents() {
        List<EventResponse> events = eventRepository.findEventSummariesByStatus("SCHEDULED").stream()
                .map(EventResponse::from)
                .toList();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}/seats")
    @Operation(summary = "이벤트 좌석 조회", description = "특정 이벤트의 전체 좌석 목록과 예매 상태를 반환합니다. AVAILABLE 좌석의 eventSeatId를 예매 요청의 eventSeatIds에 사용하세요.")
    public ResponseEntity<List<EventSeatResponse>> getSeats(@PathVariable Long eventId) {
        List<EventSeatResponse> seats = eventSeatRepository.findByEventId(eventId).stream()
                .map(EventSeatResponse::from)
                .toList();
        return ResponseEntity.ok(seats);
    }
}