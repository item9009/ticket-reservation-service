CREATE TABLE reservations
(
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    reservation_no VARCHAR(30) NOT NULL,
    user_id        BIGINT      NOT NULL,
    event_id       BIGINT      NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_amount   INT         NOT NULL,
    expires_at     DATETIME(6) NOT NULL,
    created_at     DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    UNIQUE KEY uq_reservation_no (reservation_no),
    INDEX idx_reservations_user (user_id),
    INDEX idx_reservations_status (status, expires_at),
    CONSTRAINT fk_reservations_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_reservations_event FOREIGN KEY (event_id) REFERENCES events (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE reservation_seats
(
    id             BIGINT NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT NOT NULL,
    event_seat_id  BIGINT NOT NULL,
    price          INT    NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_rseat_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (id),
    CONSTRAINT fk_rseat_event_seat FOREIGN KEY (event_seat_id) REFERENCES event_seats (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE payments
(
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT       NOT NULL,
    payment_key    VARCHAR(100) NOT NULL,
    amount         INT          NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    method         VARCHAR(30),
    paid_at        DATETIME(6),
    created_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    UNIQUE KEY uq_payment_key (payment_key),
    UNIQUE KEY uq_payment_reservation (reservation_id),
    CONSTRAINT fk_payments_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE tickets
(
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT      NOT NULL,
    event_seat_id  BIGINT      NOT NULL,
    ticket_code    VARCHAR(50) NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'VALID',
    issued_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    UNIQUE KEY uq_ticket_code (ticket_code),
    INDEX idx_tickets_reservation (reservation_id),
    CONSTRAINT fk_tickets_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (id),
    CONSTRAINT fk_tickets_event_seat FOREIGN KEY (event_seat_id) REFERENCES event_seats (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
