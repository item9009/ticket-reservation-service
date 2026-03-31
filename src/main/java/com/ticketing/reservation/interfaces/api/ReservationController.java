package com.ticketing.reservation.interfaces.api;

import com.ticketing.reservation.application.ReservationFacade;
import com.ticketing.reservation.application.dto.ReservationRequest;
import com.ticketing.reservation.application.dto.ReservationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@Tag(name = "예매 API", description = "티켓 예매 관련 API")
public class ReservationController {

    private final ReservationFacade reservationFacade;

    @PostMapping
    @Operation(summary = "좌석 예매", description = "선택한 좌석을 예매합니다. Redis 분산 락으로 동시성을 제어하며, 예매 후 10분 이내 결제하지 않으면 자동 만료됩니다.")
    public ResponseEntity<ReservationResponse> reserve(
            @Valid @RequestBody ReservationRequest request) {
        return ResponseEntity.ok(reservationFacade.reserve(request));
    }

    @GetMapping("/{reservationNo}")
    @Operation(summary = "예매 단건 조회", description = "예매 번호로 특정 예매 내역을 조회합니다.")
    public ResponseEntity<ReservationResponse> getReservation(
            @PathVariable String reservationNo) {
        return ResponseEntity.ok(reservationFacade.getReservation(reservationNo));
    }

    @GetMapping("/my/{userId}")
    @Operation(summary = "내 예매 목록 조회", description = "사용자 ID로 본인의 전체 예매 내역을 조회합니다.")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(
            @PathVariable Long userId) {
        return ResponseEntity.ok(reservationFacade.getMyReservations(userId));
    }
}
