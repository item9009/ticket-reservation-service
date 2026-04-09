-- 테스트 유저
INSERT INTO users (email, password_hash, name, phone, role)
VALUES ('test@test.com', 'hashed_password', '테스트유저', '010-1234-5678', 'USER');

-- 공연장
INSERT INTO venues (name, address, total_capacity)
VALUES ('올림픽공원 체조경기장', '서울 송파구 올림픽로 424', 15000);

-- 구역
INSERT INTO sections (venue_id, name, capacity, grade)
VALUES (1, 'VIP석', 500, 'VIP'),
       (1, 'R석', 2000, 'R'),
       (1, 'S석', 5000, 'S');

-- 좌석 (각 구역에 5개씩)
INSERT INTO seats (section_id, row_num, seat_num)
VALUES (1, 'A', '1'), (1, 'A', '2'), (1, 'A', '3'), (1, 'A', '4'), (1, 'A', '5'),
       (2, 'A', '1'), (2, 'A', '2'), (2, 'A', '3'), (2, 'A', '4'), (2, 'A', '5'),
       (3, 'A', '1'), (3, 'A', '2'), (3, 'A', '3'), (3, 'A', '4'), (3, 'A', '5');

-- 이벤트
INSERT INTO events (venue_id, title, description, event_date, booking_start_at, booking_end_at, status)
VALUES (1, '2026 봄 콘서트', '봄을 맞이하는 특별 콘서트', '2026-06-01 19:00:00', '2026-04-01 10:00:00', '2026-05-31 23:59:59', 'SCHEDULED');

-- 이벤트 좌석 (event_id=1, seat_id 1~15)
INSERT INTO event_seats (event_id, seat_id, price, status)
VALUES (1, 1,  150000, 'AVAILABLE'),
       (1, 2,  150000, 'AVAILABLE'),
       (1, 3,  150000, 'AVAILABLE'),
       (1, 4,  150000, 'AVAILABLE'),
       (1, 5,  150000, 'AVAILABLE'),
       (1, 6,  100000, 'AVAILABLE'),
       (1, 7,  100000, 'AVAILABLE'),
       (1, 8,  100000, 'AVAILABLE'),
       (1, 9,  100000, 'AVAILABLE'),
       (1, 10, 100000, 'AVAILABLE'),
       (1, 11,  70000, 'AVAILABLE'),
       (1, 12,  70000, 'AVAILABLE'),
       (1, 13,  70000, 'AVAILABLE'),
       (1, 14,  70000, 'AVAILABLE'),
       (1, 15,  70000, 'AVAILABLE');