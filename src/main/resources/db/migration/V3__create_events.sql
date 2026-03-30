CREATE TABLE events
(
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    venue_id         BIGINT       NOT NULL,
    title            VARCHAR(200) NOT NULL,
    description      TEXT,
    event_date       DATETIME(6)  NOT NULL,
    booking_start_at DATETIME(6)  NOT NULL,
    booking_end_at   DATETIME(6)  NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'SCHEDULED',
    created_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    CONSTRAINT fk_events_venue FOREIGN KEY (venue_id) REFERENCES venues (id),
    INDEX idx_events_status_date (status, event_date)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE event_seats
(
    id       BIGINT      NOT NULL AUTO_INCREMENT,
    event_id BIGINT      NOT NULL,
    seat_id  BIGINT      NOT NULL,
    price    INT         NOT NULL,
    status   VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',

    PRIMARY KEY (id),
    CONSTRAINT fk_event_seats_event FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT fk_event_seats_seat FOREIGN KEY (seat_id) REFERENCES seats (id),
    UNIQUE KEY uq_event_seat (event_id, seat_id),
    INDEX idx_event_seats_status (event_id, status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
