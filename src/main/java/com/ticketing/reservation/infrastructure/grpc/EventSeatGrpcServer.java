package com.ticketing.reservation.infrastructure.grpc;

import com.ticketing.grpc.EventSeatServiceGrpc;
import com.ticketing.grpc.ValidateSeatRequest;
import com.ticketing.grpc.ValidateSeatResponse;
import com.ticketing.grpc.SeatInfo;
import com.ticketing.reservation.domain.repository.EventSeatRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class EventSeatGrpcServer extends EventSeatServiceGrpc.EventSeatServiceImplBase {

    private final EventSeatRepository eventSeatRepository;

    @Override
    public void validateAndGetSeats(ValidateSeatRequest request,
                                    StreamObserver<ValidateSeatResponse> responseObserver) {
        log.info("gRPC 좌석 검증 요청: eventId={}, seatIds={}",
                request.getEventId(), request.getEventSeatIdsList());

        ValidateSeatResponse.Builder responseBuilder = ValidateSeatResponse.newBuilder();

        eventSeatRepository.findByEventIdAndIdIn(
                request.getEventId(),
                request.getEventSeatIdsList()
        ).forEach(eventSeat -> {
            SeatInfo seatInfo = SeatInfo.newBuilder()
                    .setEventSeatId(eventSeat.getId())
                    .setPrice(eventSeat.getPrice())
                    .setStatus(eventSeat.getStatus())
                    .build();
            responseBuilder.addSeats(seatInfo);
        });

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}
