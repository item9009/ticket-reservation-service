package com.ticketing.reservation.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class ReservationRequest {

    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;

    @NotNull(message = "이벤트 ID는 필수입니다")
    private Long eventId;

    @NotEmpty(message = "좌석을 1개 이상 선택해야 합니다")
    private List<Long> eventSeatIds;

    // 분산 락 키 생성 — 정렬 후 조합해서 데드락 방지
    public String sortedSeatKey() {
        return eventSeatIds.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }
}
