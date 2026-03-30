package com.ticketing.reservation.infrastructure.grpc;

import com.ticketing.reservation.application.dto.EventSeatInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Event Service와의 gRPC 통신 클라이언트.
 * 3단계(gRPC 구현) 전까지는 Mock으로 동작.
 * proto 파일 생성 후 실제 stub으로 교체 예정.
 */
@Slf4j
@Component
public class EventSeatGrpcClient {

    public List<EventSeatInfo> validateAndGetSeats(Long eventId, List<Long> eventSeatIds) {
        log.info("gRPC 좌석 검증 요청 (Mock): eventId={}, seatIds={}", eventId, eventSeatIds);

        // TODO: 3단계에서 실제 gRPC 호출로 교체
        // EventServiceGrpc.EventServiceBlockingStub stub = ...
        // ValidateSeatRequest request = ValidateSeatRequest.newBuilder()...

        return eventSeatIds.stream()
                .map(seatId -> new EventSeatInfo(seatId, 150000, "AVAILABLE"))
                .collect(Collectors.toList());
    }
}
