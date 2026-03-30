package com.ticketing.reservation.interfaces.api;

import com.ticketing.reservation.application.ReservationFacade;
import com.ticketing.reservation.application.dto.ReservationRequest;
import com.ticketing.reservation.application.dto.ReservationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationFacade reservationFacade;

    @PostMapping
    public ResponseEntity<ReservationResponse> reserve(
            @Valid @RequestBody ReservationRequest request) {
        return ResponseEntity.ok(reservationFacade.reserve(request));
    }

    @GetMapping("/{reservationNo}")
    public ResponseEntity<ReservationResponse> getReservation(
            @PathVariable String reservationNo) {
        return ResponseEntity.ok(reservationFacade.getReservation(reservationNo));
    }

    @GetMapping("/my/{userId}")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(
            @PathVariable Long userId) {
        return ResponseEntity.ok(reservationFacade.getMyReservations(userId));
    }
}
