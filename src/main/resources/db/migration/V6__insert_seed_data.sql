-- 테스트 유저
INSERT INTO users (email, password_hash, name, phone, role)
VALUES ('test@test.com', 'hashed_password', '테스트유저', '010-1234-5678', 'USER');

-- 공연장
INSERT INTO venues (name, address, total_capacity)
VALUES ('올림픽공원 체조경기장', '서울 송파구 올림픽로 424', 15000);

-- 구역
INSERT INTO sections (venue_id, name, capacity, grade)
VALUES ((SELECT id FROM venues WHERE name = '올림픽공원 체조경기장'), 'VIP석', 500, 'VIP'),
       ((SELECT id FROM venues WHERE name = '올림픽공원 체조경기장'), 'R석', 2000, 'R'),
       ((SELECT id FROM venues WHERE name = '올림픽공원 체조경기장'), 'S석', 5000, 'S');

-- 좌석 (각 구역에 5개씩)
INSERT INTO seats (section_id, row_num, seat_num)
VALUES ((SELECT id FROM sections WHERE name = 'VIP석'), 'A', '1'),
       ((SELECT id FROM sections WHERE name = 'VIP석'), 'A', '2'),
       ((SELECT id FROM sections WHERE name = 'VIP석'), 'A', '3'),
       ((SELECT id FROM sections WHERE name = 'VIP석'), 'A', '4'),
       ((SELECT id FROM sections WHERE name = 'VIP석'), 'A', '5'),
       ((SELECT id FROM sections WHERE name = 'R석'), 'A', '1'),
       ((SELECT id FROM sections WHERE name = 'R석'), 'A', '2'),
       ((SELECT id FROM sections WHERE name = 'R석'), 'A', '3'),
       ((SELECT id FROM sections WHERE name = 'R석'), 'A', '4'),
       ((SELECT id FROM sections WHERE name = 'R석'), 'A', '5'),
       ((SELECT id FROM sections WHERE name = 'S석'), 'A', '1'),
       ((SELECT id FROM sections WHERE name = 'S석'), 'A', '2'),
       ((SELECT id FROM sections WHERE name = 'S석'), 'A', '3'),
       ((SELECT id FROM sections WHERE name = 'S석'), 'A', '4'),
       ((SELECT id FROM sections WHERE name = 'S석'), 'A', '5');

-- 이벤트
INSERT INTO events (venue_id, title, description, event_date, booking_start_at, booking_end_at, status)
VALUES ((SELECT id FROM venues WHERE name = '올림픽공원 체조경기장'), '2026 봄 콘서트', '봄을 맞이하는 특별 콘서트', '2026-06-01 19:00:00', '2026-04-01 10:00:00', '2026-05-31 23:59:59', 'SCHEDULED');

-- 이벤트 좌석
INSERT INTO event_seats (event_id, seat_id, price, status)
SELECT
    (SELECT id FROM events WHERE title = '2026 봄 콘서트'),
    s.id,
    CASE sec.grade
        WHEN 'VIP' THEN 150000
        WHEN 'R'   THEN 100000
        ELSE 70000
    END,
    'AVAILABLE'
FROM seats s
JOIN sections sec ON s.section_id = sec.id
WHERE sec.venue_id = (SELECT id FROM venues WHERE name = '올림픽공원 체조경기장');
