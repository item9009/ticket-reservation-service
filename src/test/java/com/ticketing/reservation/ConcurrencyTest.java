package com.ticketing.reservation;

import com.ticketing.reservation.domain.repository.ReservationRepository;
import com.ticketing.reservation.domain.repository.ReservationSeatRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ConcurrencyTest {

    @Autowired
    private ReservationSeatRepository reservationSeatRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        reservationSeatRepository.deleteAll();
        reservationRepository.deleteAll();
    }

    @Test
    @DisplayName("100명이 동시에 같은 좌석 예매 시 1명만 성공해야 한다")
    void concurrencyTest() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        HttpClient client = HttpClient.newHttpClient();
        String requestBody = """
                {
                    "userId": 1,
                    "eventId": 1,
                    "eventSeatIds": [1]
                }
                """;

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8080/api/v1/reservations"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                            .build();

                    HttpResponse<String> response = client.send(
                            request, HttpResponse.BodyHandlers.ofString()
                    );

                    if (response.statusCode() == 200) {
                        successCount.incrementAndGet();
                        log.info("예매 성공 - status: {}", response.statusCode());
                    } else {
                        failCount.incrementAndGet();
                        log.info("예매 실패 - status: {}", response.statusCode());
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    log.error("예외 발생: {}", e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        log.info("==============================");
        log.info("성공: {}", successCount.get());
        log.info("실패: {}", failCount.get());
        log.info("==============================");

        assert successCount.get() <= 1 : "동시성 제어 실패! 성공 건수: " + successCount.get();
    }
}
