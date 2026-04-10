package com.ticketing.reservation.infrastructure.grpc;

import com.ticketing.grpc.EventSeatServiceGrpc;
import com.ticketing.grpc.ValidateSeatRequest;
import com.ticketing.grpc.ValidateSeatResponse;
import com.ticketing.reservation.application.dto.EventSeatInfo;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class EventSeatGrpcClient {

    @GrpcClient("event-service")
    private EventSeatServiceGrpc.EventSeatServiceBlockingStub eventSeatStub;

    public List<EventSeatInfo> validateAndGetSeats(Long eventId, List<Long> eventSeatIds) {
        log.info("gRPC 좌석 검증 요청: eventId={}, seatIds={}", eventId, eventSeatIds);

        ValidateSeatRequest request = ValidateSeatRequest.newBuilder()
                .setEventId(eventId)
                .addAllEventSeatIds(eventSeatIds)
                .build();

        ValidateSeatResponse response = eventSeatStub.validateAndGetSeats(request);

        return response.getSeatsList().stream()
                .map(seat -> new EventSeatInfo(
                        seat.getEventSeatId(),
                        seat.getPrice(),
                        seat.getStatus()
                ))
                .collect(Collectors.toList());
    }
}
