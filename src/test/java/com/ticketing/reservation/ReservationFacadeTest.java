package com.ticketing.reservation.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.reservation.application.dto.EventSeatInfo;
import com.ticketing.reservation.application.dto.ReservationRequest;
import com.ticketing.reservation.application.dto.ReservationResponse;
import com.ticketing.reservation.domain.entity.Reservation;
import com.ticketing.reservation.domain.entity.ReservationStatus;
import com.ticketing.reservation.domain.repository.OutboxEventRepository;
import com.ticketing.reservation.domain.repository.ReservationRepository;
import com.ticketing.reservation.domain.service.ReservationDomainService;
import com.ticketing.reservation.infrastructure.grpc.EventSeatGrpcClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationFacadeTest {

    @InjectMocks
    private ReservationFacade reservationFacade;

    @Mock
    private ReservationDomainService reservationDomainService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private EventSeatGrpcClient eventSeatGrpcClient;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("정상 예매 시 PENDING 상태의 예매가 생성된다")
    void reserve_success() throws Exception {
        // given
        ReservationRequest request = new ReservationRequest();
        // reflection으로 필드 주입 (Lombok @NoArgsConstructor 활용)
        setField(request, "userId", 1L);
        setField(request, "eventId", 10L);
        setField(request, "eventSeatIds", List.of(101L, 102L));

        List<EventSeatInfo> seatInfos = List.of(
                new EventSeatInfo(101L, 150000, "AVAILABLE"),
                new EventSeatInfo(102L, 150000, "AVAILABLE")
        );

        Reservation mockReservation = Reservation.create(1L, 10L, 300000);

        when(eventSeatGrpcClient.validateAndGetSeats(anyLong(), anyList())).thenReturn(seatInfos);
        when(reservationDomainService.createReservation(anyLong(), anyLong(), anyList()))
                .thenReturn(mockReservation);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // when
        ReservationResponse response = reservationFacade.reserve(request);

        // then
        assertThat(response.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(response.getTotalAmount()).isEqualTo(300000);
        verify(outboxEventRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("이미 선점된 좌석 예매 시 예외가 발생한다")
    void reserve_soldOutSeat_throwsException() {
        // given
        ReservationRequest request = new ReservationRequest();
        setField(request, "userId", 1L);
        setField(request, "eventId", 10L);
        setField(request, "eventSeatIds", List.of(101L));

        List<EventSeatInfo> seatInfos = List.of(
                new EventSeatInfo(101L, 150000, "RESERVED")  // 이미 선점된 좌석
        );

        when(eventSeatGrpcClient.validateAndGetSeats(anyLong(), anyList())).thenReturn(seatInfos);
        when(reservationDomainService.createReservation(anyLong(), anyLong(), anyList()))
                .thenThrow(new IllegalStateException("이미 선점된 좌석입니다: eventSeatId=101"));

        // when & then
        assertThatThrownBy(() -> reservationFacade.reserve(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 선점된 좌석");
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
