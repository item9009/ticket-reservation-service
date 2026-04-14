package com.ticketing.reservation.interfaces.api;

import com.ticketing.reservation.application.dto.EventCreateRequest;
import com.ticketing.reservation.application.dto.EventResponse;
import com.ticketing.reservation.application.dto.EventUpdateRequest;
import com.ticketing.reservation.domain.entity.Event;
import com.ticketing.reservation.domain.entity.EventSeat;
import com.ticketing.reservation.domain.repository.EventRepository;
import com.ticketing.reservation.domain.repository.EventSeatRepository;
import com.ticketing.reservation.domain.repository.SeatRepository;
import com.ticketing.reservation.domain.repository.SeatRepository.SeatGradeProjection;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/events")
@RequiredArgsConstructor
@Tag(name = "관리자 이벤트 API", description = "공연 등록/수정/삭제 API (ADMIN 전용)")
public class AdminEventController {

    private final EventRepository eventRepository;
    private final EventSeatRepository eventSeatRepository;
    private final SeatRepository seatRepository;

    @PostMapping
    @Operation(summary = "공연 등록", description = "새 공연을 등록하고 venue의 모든 좌석을 grade별 가격으로 자동 생성합니다.")
    public ResponseEntity<EventResponse> createEvent(@RequestBody EventCreateRequest request) {
        Event event = eventRepository.save(
                Event.createNew(
                        request.getVenueId(),
                        request.getTitle(),
                        request.getDescription(),
                        request.getEventDate(),
                        request.getBookingStartAt(),
                        request.getBookingEndAt(),
                        "SCHEDULED"
                )
        );

        Map<String, Integer> gradePrices = request.getGradePrices();
        List<SeatGradeProjection> seats = seatRepository.findSeatGradesByVenueId(request.getVenueId());

        List<EventSeat> eventSeats = seats.stream()
                .map(seat -> EventSeat.createNew(
                        event.getId(),
                        seat.getSeatId(),
                        seat.getGrade(),
                        gradePrices.getOrDefault(seat.getGrade(), 0)
                ))
                .toList();

        eventSeatRepository.saveAll(eventSeats);

        EventResponse response = EventResponse.from(event);
        return ResponseEntity.created(URI.create("/api/v1/events/" + event.getId())).body(response);
    }

    @PutMapping("/{eventId}")
    @Operation(summary = "공연 수정", description = "공연 정보를 수정합니다.")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable Long eventId,
                                                     @RequestBody EventUpdateRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다: " + eventId));

        event.update(
                request.getTitle(),
                request.getDescription(),
                request.getEventDate(),
                request.getBookingStartAt(),
                request.getBookingEndAt(),
                request.getStatus()
        );

        eventRepository.save(event);
        return ResponseEntity.ok(EventResponse.from(event));
    }

    @DeleteMapping("/{eventId}")
    @Operation(summary = "공연 취소", description = "공연 상태를 CANCELLED로 변경합니다.")
    public ResponseEntity<Void> cancelEvent(@PathVariable Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다: " + eventId));

        event.update(
                event.getTitle(),
                event.getDescription(),
                event.getEventDate(),
                event.getBookingStartAt(),
                event.getBookingEndAt(),
                "CANCELLED"
        );

        eventRepository.save(event);
        return ResponseEntity.noContent().build();
    }
}
