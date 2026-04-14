-- event_seats 테이블에 grade 컬럼 추가
ALTER TABLE event_seats
    ADD COLUMN grade VARCHAR(10) NOT NULL DEFAULT 'S' AFTER price;

-- 기존 event_seats의 grade를 sections에서 채워넣기
UPDATE event_seats es
    JOIN seats s ON es.seat_id = s.id
    JOIN sections sec ON s.section_id = sec.id
SET es.grade = sec.grade;

-- VIP석 추가 (기존 A열 5개 → B열 5개 추가 = 총 10개)
INSERT INTO seats (section_id, row_num, seat_num)
SELECT sec.id, 'B', n.num
FROM sections sec
         CROSS JOIN (SELECT '1' AS num
                     UNION SELECT '2'
                     UNION SELECT '3'
                     UNION SELECT '4'
                     UNION SELECT '5') n
WHERE sec.name = 'VIP석';

-- R석 추가 (기존 A열 5개 → B~F열 추가 = 총 30개)
INSERT INTO seats (section_id, row_num, seat_num)
SELECT sec.id, r.row_num, n.num
FROM sections sec
         CROSS JOIN (SELECT 'B' AS row_num
                     UNION SELECT 'C'
                     UNION SELECT 'D'
                     UNION SELECT 'E'
                     UNION SELECT 'F') r
         CROSS JOIN (SELECT '1' AS num
                     UNION SELECT '2'
                     UNION SELECT '3'
                     UNION SELECT '4'
                     UNION SELECT '5') n
WHERE sec.name = 'R석';

-- S석 추가 (기존 A열 5개 → B~L열 추가 = 총 60개)
INSERT INTO seats (section_id, row_num, seat_num)
SELECT sec.id, r.row_num, n.num
FROM sections sec
         CROSS JOIN (SELECT 'B' AS row_num
                     UNION SELECT 'C'
                     UNION SELECT 'D'
                     UNION SELECT 'E'
                     UNION SELECT 'F'
                     UNION SELECT 'G'
                     UNION SELECT 'H'
                     UNION SELECT 'I'
                     UNION SELECT 'J'
                     UNION SELECT 'K'
                     UNION SELECT 'L') r
         CROSS JOIN (SELECT '1' AS num
                     UNION SELECT '2'
                     UNION SELECT '3'
                     UNION SELECT '4'
                     UNION SELECT '5') n
WHERE sec.name = 'S석';

-- 새로 추가된 좌석을 event_seats에 등록
INSERT INTO event_seats (event_id, seat_id, price, grade, status)
SELECT e.id,
       s.id,
       CASE sec.grade
           WHEN 'VIP' THEN 150000
           WHEN 'R' THEN 100000
           ELSE 70000
           END,
       sec.grade,
       'AVAILABLE'
FROM seats s
         JOIN sections sec ON s.section_id = sec.id
         JOIN events e ON e.title = '2026 봄 콘서트'
         LEFT JOIN event_seats existing ON existing.seat_id = s.id AND existing.event_id = e.id
WHERE sec.venue_id = (SELECT id FROM venues WHERE name = '올림픽공원 체조경기장')
  AND existing.id IS NULL;
