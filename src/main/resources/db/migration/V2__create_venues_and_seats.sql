CREATE TABLE venues
(
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    name           VARCHAR(100) NOT NULL,
    address        VARCHAR(255) NOT NULL,
    total_capacity INT          NOT NULL,
    created_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE sections
(
    id       BIGINT      NOT NULL AUTO_INCREMENT,
    venue_id BIGINT      NOT NULL,
    name     VARCHAR(50) NOT NULL,
    capacity INT         NOT NULL,
    grade    VARCHAR(20) NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_sections_venue FOREIGN KEY (venue_id) REFERENCES venues (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE seats
(
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    section_id BIGINT      NOT NULL,
    row_num    VARCHAR(10) NOT NULL,
    seat_num   VARCHAR(10) NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_seats_section FOREIGN KEY (section_id) REFERENCES sections (id),
    UNIQUE KEY uq_seat_position (section_id, row_num, seat_num)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
