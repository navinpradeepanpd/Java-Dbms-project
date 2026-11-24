-- ===================================================================
-- CineBook: Movie Ticket Booking System - Seed Data
-- ===================================================================

USE cinebook_db;

-- 1. SEED MOVIES (7 Movies across diverse genres)
INSERT INTO movie (movie_id, movie_name, duration, language, genre) VALUES
(1, 'Inception', 148, 'English', 'Sci-Fi / Thriller'),
(2, 'Interstellar', 169, 'English', 'Sci-Fi / Adventure'),
(3, 'The Dark Knight', 152, 'English', 'Action / Crime'),
(4, 'Oppenheimer', 180, 'English', 'Biography / Drama'),
(5, 'Avatar: The Way of Water', 192, 'English', 'Sci-Fi / Adventure'),
(6, 'Gladiator II', 148, 'English', 'Action / Drama'),
(7, 'Spider-Man: Across the Spider-Verse', 140, 'English', 'Animation / Action')
ON DUPLICATE KEY UPDATE movie_name=VALUES(movie_name), duration=VALUES(duration), language=VALUES(language), genre=VALUES(genre);

-- 2. SEED SHOWS (8 Movie Shows across multiple screens)
INSERT INTO movie_show (show_id, movie_id, show_date, show_time, screen_name) VALUES
(1, 1, '2026-11-05', '14:00:00', 'Screen 1'),
(2, 2, '2026-11-05', '18:30:00', 'Screen 2'),
(3, 3, '2026-11-06', '20:00:00', 'Screen 1'),
(4, 4, '2026-11-06', '15:30:00', 'Screen 2'),
(5, 4, '2026-11-07', '19:45:00', 'Screen 1'),
(6, 5, '2026-11-06', '18:00:00', 'Screen 3'),
(7, 6, '2026-11-07', '14:15:00', 'Screen 2'),
(8, 7, '2026-11-07', '17:30:00', 'Screen 3')
ON DUPLICATE KEY UPDATE show_time=VALUES(show_time), screen_name=VALUES(screen_name);

-- 3. SEED 5x8 SEAT LAYOUT FOR SHOW 1 (Inception - 14:00)
INSERT IGNORE INTO seat (show_id, seat_code, seat_type, status) VALUES
-- Row A (Regular)
(1, 'A1', 'REGULAR', 'AVAILABLE'), (1, 'A2', 'REGULAR', 'AVAILABLE'), (1, 'A3', 'REGULAR', 'AVAILABLE'), (1, 'A4', 'REGULAR', 'AVAILABLE'),
(1, 'A5', 'REGULAR', 'AVAILABLE'), (1, 'A6', 'REGULAR', 'AVAILABLE'), (1, 'A7', 'REGULAR', 'AVAILABLE'), (1, 'A8', 'REGULAR', 'AVAILABLE'),
-- Row B (Regular)
(1, 'B1', 'REGULAR', 'AVAILABLE'), (1, 'B2', 'REGULAR', 'AVAILABLE'), (1, 'B3', 'REGULAR', 'AVAILABLE'), (1, 'B4', 'REGULAR', 'AVAILABLE'),
(1, 'B5', 'REGULAR', 'AVAILABLE'), (1, 'B6', 'REGULAR', 'AVAILABLE'), (1, 'B7', 'REGULAR', 'AVAILABLE'), (1, 'B8', 'REGULAR', 'AVAILABLE'),
-- Row C (Premium)
(1, 'C1', 'PREMIUM', 'AVAILABLE'), (1, 'C2', 'PREMIUM', 'AVAILABLE'), (1, 'C3', 'PREMIUM', 'AVAILABLE'), (1, 'C4', 'PREMIUM', 'AVAILABLE'),
(1, 'C5', 'PREMIUM', 'AVAILABLE'), (1, 'C6', 'PREMIUM', 'AVAILABLE'), (1, 'C7', 'PREMIUM', 'AVAILABLE'), (1, 'C8', 'PREMIUM', 'AVAILABLE'),
-- Row D (Premium)
(1, 'D1', 'PREMIUM', 'AVAILABLE'), (1, 'D2', 'PREMIUM', 'AVAILABLE'), (1, 'D3', 'PREMIUM', 'AVAILABLE'), (1, 'D4', 'PREMIUM', 'AVAILABLE'),
(1, 'D5', 'PREMIUM', 'AVAILABLE'), (1, 'D6', 'PREMIUM', 'AVAILABLE'), (1, 'D7', 'PREMIUM', 'AVAILABLE'), (1, 'D8', 'PREMIUM', 'AVAILABLE'),
-- Row E (VIP)
(1, 'E1', 'VIP', 'AVAILABLE'), (1, 'E2', 'VIP', 'AVAILABLE'), (1, 'E3', 'VIP', 'AVAILABLE'), (1, 'E4', 'VIP', 'AVAILABLE'),
(1, 'E5', 'VIP', 'AVAILABLE'), (1, 'E6', 'VIP', 'AVAILABLE'), (1, 'E7', 'VIP', 'AVAILABLE'), (1, 'E8', 'VIP', 'AVAILABLE');

-- 4. SEED 5x8 SEAT LAYOUT FOR SHOW 2 (Interstellar - 18:30)
INSERT IGNORE INTO seat (show_id, seat_code, seat_type, status) VALUES
-- Row A (Regular)
(2, 'A1', 'REGULAR', 'AVAILABLE'), (2, 'A2', 'REGULAR', 'AVAILABLE'), (2, 'A3', 'REGULAR', 'AVAILABLE'), (2, 'A4', 'REGULAR', 'AVAILABLE'),
(2, 'A5', 'REGULAR', 'AVAILABLE'), (2, 'A6', 'REGULAR', 'AVAILABLE'), (2, 'A7', 'REGULAR', 'AVAILABLE'), (2, 'A8', 'REGULAR', 'AVAILABLE'),
-- Row B (Regular)
(2, 'B1', 'REGULAR', 'AVAILABLE'), (2, 'B2', 'REGULAR', 'AVAILABLE'), (2, 'B3', 'REGULAR', 'AVAILABLE'), (2, 'B4', 'REGULAR', 'AVAILABLE'),
(2, 'B5', 'REGULAR', 'AVAILABLE'), (2, 'B6', 'REGULAR', 'AVAILABLE'), (2, 'B7', 'REGULAR', 'AVAILABLE'), (2, 'B8', 'REGULAR', 'AVAILABLE'),
-- Row C (Premium)
(2, 'C1', 'PREMIUM', 'AVAILABLE'), (2, 'C2', 'PREMIUM', 'AVAILABLE'), (2, 'C3', 'PREMIUM', 'AVAILABLE'), (2, 'C4', 'PREMIUM', 'AVAILABLE'),
(2, 'C5', 'PREMIUM', 'AVAILABLE'), (2, 'C6', 'PREMIUM', 'AVAILABLE'), (2, 'C7', 'PREMIUM', 'AVAILABLE'), (2, 'C8', 'PREMIUM', 'AVAILABLE'),
-- Row D (Premium)
(2, 'D1', 'PREMIUM', 'AVAILABLE'), (2, 'D2', 'PREMIUM', 'AVAILABLE'), (2, 'D3', 'PREMIUM', 'AVAILABLE'), (2, 'D4', 'PREMIUM', 'AVAILABLE'),
(2, 'D5', 'PREMIUM', 'AVAILABLE'), (2, 'D6', 'PREMIUM', 'AVAILABLE'), (2, 'D7', 'PREMIUM', 'AVAILABLE'), (2, 'D8', 'PREMIUM', 'AVAILABLE'),
-- Row E (VIP)
(2, 'E1', 'VIP', 'AVAILABLE'), (2, 'E2', 'VIP', 'AVAILABLE'), (2, 'E3', 'VIP', 'AVAILABLE'), (2, 'E4', 'VIP', 'AVAILABLE'),
(2, 'E5', 'VIP', 'AVAILABLE'), (2, 'E6', 'VIP', 'AVAILABLE'), (2, 'E7', 'VIP', 'AVAILABLE'), (2, 'E8', 'VIP', 'AVAILABLE');

-- 5. SEED 5x8 SEAT LAYOUT FOR SHOW 3 (The Dark Knight - 20:00)
INSERT IGNORE INTO seat (show_id, seat_code, seat_type, status) VALUES
-- Row A (Regular)
(3, 'A1', 'REGULAR', 'AVAILABLE'), (3, 'A2', 'REGULAR', 'AVAILABLE'), (3, 'A3', 'REGULAR', 'AVAILABLE'), (3, 'A4', 'REGULAR', 'AVAILABLE'),
(3, 'A5', 'REGULAR', 'AVAILABLE'), (3, 'A6', 'REGULAR', 'AVAILABLE'), (3, 'A7', 'REGULAR', 'AVAILABLE'), (3, 'A8', 'REGULAR', 'AVAILABLE'),
-- Row B (Regular)
(3, 'B1', 'REGULAR', 'AVAILABLE'), (3, 'B2', 'REGULAR', 'AVAILABLE'), (3, 'B3', 'REGULAR', 'AVAILABLE'), (3, 'B4', 'REGULAR', 'AVAILABLE'),
(3, 'B5', 'REGULAR', 'AVAILABLE'), (3, 'B6', 'REGULAR', 'AVAILABLE'), (3, 'B7', 'REGULAR', 'AVAILABLE'), (3, 'B8', 'REGULAR', 'AVAILABLE'),
-- Row C (Premium)
(3, 'C1', 'PREMIUM', 'AVAILABLE'), (3, 'C2', 'PREMIUM', 'AVAILABLE'), (3, 'C3', 'PREMIUM', 'AVAILABLE'), (3, 'C4', 'PREMIUM', 'AVAILABLE'),
(3, 'C5', 'PREMIUM', 'AVAILABLE'), (3, 'C6', 'PREMIUM', 'AVAILABLE'), (3, 'C7', 'PREMIUM', 'AVAILABLE'), (3, 'C8', 'PREMIUM', 'AVAILABLE'),
-- Row D (Premium)
(3, 'D1', 'PREMIUM', 'AVAILABLE'), (3, 'D2', 'PREMIUM', 'AVAILABLE'), (3, 'D3', 'PREMIUM', 'AVAILABLE'), (3, 'D4', 'PREMIUM', 'AVAILABLE'),
(3, 'D5', 'PREMIUM', 'AVAILABLE'), (3, 'D6', 'PREMIUM', 'AVAILABLE'), (3, 'D7', 'PREMIUM', 'AVAILABLE'), (3, 'D8', 'PREMIUM', 'AVAILABLE'),
-- Row E (VIP)
(3, 'E1', 'VIP', 'AVAILABLE'), (3, 'E2', 'VIP', 'AVAILABLE'), (3, 'E3', 'VIP', 'AVAILABLE'), (3, 'E4', 'VIP', 'AVAILABLE'),
(3, 'E5', 'VIP', 'AVAILABLE'), (3, 'E6', 'VIP', 'AVAILABLE'), (3, 'E7', 'VIP', 'AVAILABLE'), (3, 'E8', 'VIP', 'AVAILABLE');

-- 6. SEED 5x8 SEAT LAYOUT FOR SHOW 4 (Oppenheimer - 15:30)
INSERT IGNORE INTO seat (show_id, seat_code, seat_type, status) VALUES
-- Row A (Regular)
(4, 'A1', 'REGULAR', 'AVAILABLE'), (4, 'A2', 'REGULAR', 'AVAILABLE'), (4, 'A3', 'REGULAR', 'AVAILABLE'), (4, 'A4', 'REGULAR', 'AVAILABLE'),
(4, 'A5', 'REGULAR', 'AVAILABLE'), (4, 'A6', 'REGULAR', 'AVAILABLE'), (4, 'A7', 'REGULAR', 'AVAILABLE'), (4, 'A8', 'REGULAR', 'AVAILABLE'),
-- Row B (Regular)
(4, 'B1', 'REGULAR', 'AVAILABLE'), (4, 'B2', 'REGULAR', 'AVAILABLE'), (4, 'B3', 'REGULAR', 'AVAILABLE'), (4, 'B4', 'REGULAR', 'AVAILABLE'),
(4, 'B5', 'REGULAR', 'AVAILABLE'), (4, 'B6', 'REGULAR', 'AVAILABLE'), (4, 'B7', 'REGULAR', 'AVAILABLE'), (4, 'B8', 'REGULAR', 'AVAILABLE'),
-- Row C (Premium)
(4, 'C1', 'PREMIUM', 'AVAILABLE'), (4, 'C2', 'PREMIUM', 'AVAILABLE'), (4, 'C3', 'PREMIUM', 'AVAILABLE'), (4, 'C4', 'PREMIUM', 'AVAILABLE'),
(4, 'C5', 'PREMIUM', 'AVAILABLE'), (4, 'C6', 'PREMIUM', 'AVAILABLE'), (4, 'C7', 'PREMIUM', 'AVAILABLE'), (4, 'C8', 'PREMIUM', 'AVAILABLE'),
-- Row D (Premium)
(4, 'D1', 'PREMIUM', 'AVAILABLE'), (4, 'D2', 'PREMIUM', 'AVAILABLE'), (4, 'D3', 'PREMIUM', 'AVAILABLE'), (4, 'D4', 'PREMIUM', 'AVAILABLE'),
(4, 'D5', 'PREMIUM', 'AVAILABLE'), (4, 'D6', 'PREMIUM', 'AVAILABLE'), (4, 'D7', 'PREMIUM', 'AVAILABLE'), (4, 'D8', 'PREMIUM', 'AVAILABLE'),
-- Row E (VIP)
(4, 'E1', 'VIP', 'AVAILABLE'), (4, 'E2', 'VIP', 'AVAILABLE'), (4, 'E3', 'VIP', 'AVAILABLE'), (4, 'E4', 'VIP', 'AVAILABLE'),
(4, 'E5', 'VIP', 'AVAILABLE'), (4, 'E6', 'VIP', 'AVAILABLE'), (4, 'E7', 'VIP', 'AVAILABLE'), (4, 'E8', 'VIP', 'AVAILABLE');

-- 7. SEED 5x8 SEAT LAYOUT FOR SHOW 5 (Oppenheimer - 19:45)
INSERT IGNORE INTO seat (show_id, seat_code, seat_type, status) VALUES
-- Row A (Regular)
(5, 'A1', 'REGULAR', 'AVAILABLE'), (5, 'A2', 'REGULAR', 'AVAILABLE'), (5, 'A3', 'REGULAR', 'AVAILABLE'), (5, 'A4', 'REGULAR', 'AVAILABLE'),
(5, 'A5', 'REGULAR', 'AVAILABLE'), (5, 'A6', 'REGULAR', 'AVAILABLE'), (5, 'A7', 'REGULAR', 'AVAILABLE'), (5, 'A8', 'REGULAR', 'AVAILABLE'),
-- Row B (Regular)
(5, 'B1', 'REGULAR', 'AVAILABLE'), (5, 'B2', 'REGULAR', 'AVAILABLE'), (5, 'B3', 'REGULAR', 'AVAILABLE'), (5, 'B4', 'REGULAR', 'AVAILABLE'),
(5, 'B5', 'REGULAR', 'AVAILABLE'), (5, 'B6', 'REGULAR', 'AVAILABLE'), (5, 'B7', 'REGULAR', 'AVAILABLE'), (5, 'B8', 'REGULAR', 'AVAILABLE'),
-- Row C (Premium)
(5, 'C1', 'PREMIUM', 'AVAILABLE'), (5, 'C2', 'PREMIUM', 'AVAILABLE'), (5, 'C3', 'PREMIUM', 'AVAILABLE'), (5, 'C4', 'PREMIUM', 'AVAILABLE'),
(5, 'C5', 'PREMIUM', 'AVAILABLE'), (5, 'C6', 'PREMIUM', 'AVAILABLE'), (5, 'C7', 'PREMIUM', 'AVAILABLE'), (5, 'C8', 'PREMIUM', 'AVAILABLE'),
-- Row D (Premium)
(5, 'D1', 'PREMIUM', 'AVAILABLE'), (5, 'D2', 'PREMIUM', 'AVAILABLE'), (5, 'D3', 'PREMIUM', 'AVAILABLE'), (5, 'D4', 'PREMIUM', 'AVAILABLE'),
(5, 'D5', 'PREMIUM', 'AVAILABLE'), (5, 'D6', 'PREMIUM', 'AVAILABLE'), (5, 'D7', 'PREMIUM', 'AVAILABLE'), (5, 'D8', 'PREMIUM', 'AVAILABLE'),
-- Row E (VIP)
(5, 'E1', 'VIP', 'AVAILABLE'), (5, 'E2', 'VIP', 'AVAILABLE'), (5, 'E3', 'VIP', 'AVAILABLE'), (5, 'E4', 'VIP', 'AVAILABLE'),
(5, 'E5', 'VIP', 'AVAILABLE'), (5, 'E6', 'VIP', 'AVAILABLE'), (5, 'E7', 'VIP', 'AVAILABLE'), (5, 'E8', 'VIP', 'AVAILABLE');

-- 8. SEED 5x8 SEAT LAYOUT FOR SHOW 6 (Avatar: The Way of Water - 18:00)
INSERT IGNORE INTO seat (show_id, seat_code, seat_type, status) VALUES
-- Row A (Regular)
(6, 'A1', 'REGULAR', 'AVAILABLE'), (6, 'A2', 'REGULAR', 'AVAILABLE'), (6, 'A3', 'REGULAR', 'AVAILABLE'), (6, 'A4', 'REGULAR', 'AVAILABLE'),
(6, 'A5', 'REGULAR', 'AVAILABLE'), (6, 'A6', 'REGULAR', 'AVAILABLE'), (6, 'A7', 'REGULAR', 'AVAILABLE'), (6, 'A8', 'REGULAR', 'AVAILABLE'),
-- Row B (Regular)
(6, 'B1', 'REGULAR', 'AVAILABLE'), (6, 'B2', 'REGULAR', 'AVAILABLE'), (6, 'B3', 'REGULAR', 'AVAILABLE'), (6, 'B4', 'REGULAR', 'AVAILABLE'),
(6, 'B5', 'REGULAR', 'AVAILABLE'), (6, 'B6', 'REGULAR', 'AVAILABLE'), (6, 'B7', 'REGULAR', 'AVAILABLE'), (6, 'B8', 'REGULAR', 'AVAILABLE'),
-- Row C (Premium)
(6, 'C1', 'PREMIUM', 'AVAILABLE'), (6, 'C2', 'PREMIUM', 'AVAILABLE'), (6, 'C3', 'PREMIUM', 'AVAILABLE'), (6, 'C4', 'PREMIUM', 'AVAILABLE'),
(6, 'C5', 'PREMIUM', 'AVAILABLE'), (6, 'C6', 'PREMIUM', 'AVAILABLE'), (6, 'C7', 'PREMIUM', 'AVAILABLE'), (6, 'C8', 'PREMIUM', 'AVAILABLE'),
-- Row D (Premium)
(6, 'D1', 'PREMIUM', 'AVAILABLE'), (6, 'D2', 'PREMIUM', 'AVAILABLE'), (6, 'D3', 'PREMIUM', 'AVAILABLE'), (6, 'D4', 'PREMIUM', 'AVAILABLE'),
(6, 'D5', 'PREMIUM', 'AVAILABLE'), (6, 'D6', 'PREMIUM', 'AVAILABLE'), (6, 'D7', 'PREMIUM', 'AVAILABLE'), (6, 'D8', 'PREMIUM', 'AVAILABLE'),
-- Row E (VIP)
(6, 'E1', 'VIP', 'AVAILABLE'), (6, 'E2', 'VIP', 'AVAILABLE'), (6, 'E3', 'VIP', 'AVAILABLE'), (6, 'E4', 'VIP', 'AVAILABLE'),
(6, 'E5', 'VIP', 'AVAILABLE'), (6, 'E6', 'VIP', 'AVAILABLE'), (6, 'E7', 'VIP', 'AVAILABLE'), (6, 'E8', 'VIP', 'AVAILABLE');

-- 9. SEED 5x8 SEAT LAYOUT FOR SHOW 7 (Gladiator II - 14:15)
INSERT IGNORE INTO seat (show_id, seat_code, seat_type, status) VALUES
-- Row A (Regular)
(7, 'A1', 'REGULAR', 'AVAILABLE'), (7, 'A2', 'REGULAR', 'AVAILABLE'), (7, 'A3', 'REGULAR', 'AVAILABLE'), (7, 'A4', 'REGULAR', 'AVAILABLE'),
(7, 'A5', 'REGULAR', 'AVAILABLE'), (7, 'A6', 'REGULAR', 'AVAILABLE'), (7, 'A7', 'REGULAR', 'AVAILABLE'), (7, 'A8', 'REGULAR', 'AVAILABLE'),
-- Row B (Regular)
(7, 'B1', 'REGULAR', 'AVAILABLE'), (7, 'B2', 'REGULAR', 'AVAILABLE'), (7, 'B3', 'REGULAR', 'AVAILABLE'), (7, 'B4', 'REGULAR', 'AVAILABLE'),
(7, 'B5', 'REGULAR', 'AVAILABLE'), (7, 'B6', 'REGULAR', 'AVAILABLE'), (7, 'B7', 'REGULAR', 'AVAILABLE'), (7, 'B8', 'REGULAR', 'AVAILABLE'),
-- Row C (Premium)
(7, 'C1', 'PREMIUM', 'AVAILABLE'), (7, 'C2', 'PREMIUM', 'AVAILABLE'), (7, 'C3', 'PREMIUM', 'AVAILABLE'), (7, 'C4', 'PREMIUM', 'AVAILABLE'),
(7, 'C5', 'PREMIUM', 'AVAILABLE'), (7, 'C6', 'PREMIUM', 'AVAILABLE'), (7, 'C7', 'PREMIUM', 'AVAILABLE'), (7, 'C8', 'PREMIUM', 'AVAILABLE'),
-- Row D (Premium)
(7, 'D1', 'PREMIUM', 'AVAILABLE'), (7, 'D2', 'PREMIUM', 'AVAILABLE'), (7, 'D3', 'PREMIUM', 'AVAILABLE'), (7, 'D4', 'PREMIUM', 'AVAILABLE'),
(7, 'D5', 'PREMIUM', 'AVAILABLE'), (7, 'D6', 'PREMIUM', 'AVAILABLE'), (7, 'D7', 'PREMIUM', 'AVAILABLE'), (7, 'D8', 'PREMIUM', 'AVAILABLE'),
-- Row E (VIP)
(7, 'E1', 'VIP', 'AVAILABLE'), (7, 'E2', 'VIP', 'AVAILABLE'), (7, 'E3', 'VIP', 'AVAILABLE'), (7, 'E4', 'VIP', 'AVAILABLE'),
(7, 'E5', 'VIP', 'AVAILABLE'), (7, 'E6', 'VIP', 'AVAILABLE'), (7, 'E7', 'VIP', 'AVAILABLE'), (7, 'E8', 'VIP', 'AVAILABLE');

-- 10. SEED 5x8 SEAT LAYOUT FOR SHOW 8 (Spider-Man: Across the Spider-Verse - 17:30)
INSERT IGNORE INTO seat (show_id, seat_code, seat_type, status) VALUES
-- Row A (Regular)
(8, 'A1', 'REGULAR', 'AVAILABLE'), (8, 'A2', 'REGULAR', 'AVAILABLE'), (8, 'A3', 'REGULAR', 'AVAILABLE'), (8, 'A4', 'REGULAR', 'AVAILABLE'),
(8, 'A5', 'REGULAR', 'AVAILABLE'), (8, 'A6', 'REGULAR', 'AVAILABLE'), (8, 'A7', 'REGULAR', 'AVAILABLE'), (8, 'A8', 'REGULAR', 'AVAILABLE'),
-- Row B (Regular)
(8, 'B1', 'REGULAR', 'AVAILABLE'), (8, 'B2', 'REGULAR', 'AVAILABLE'), (8, 'B3', 'REGULAR', 'AVAILABLE'), (8, 'B4', 'REGULAR', 'AVAILABLE'),
(8, 'B5', 'REGULAR', 'AVAILABLE'), (8, 'B6', 'REGULAR', 'AVAILABLE'), (8, 'B7', 'REGULAR', 'AVAILABLE'), (8, 'B8', 'REGULAR', 'AVAILABLE'),
-- Row C (Premium)
(8, 'C1', 'PREMIUM', 'AVAILABLE'), (8, 'C2', 'PREMIUM', 'AVAILABLE'), (8, 'C3', 'PREMIUM', 'AVAILABLE'), (8, 'C4', 'PREMIUM', 'AVAILABLE'),
(8, 'C5', 'PREMIUM', 'AVAILABLE'), (8, 'C6', 'PREMIUM', 'AVAILABLE'), (8, 'C7', 'PREMIUM', 'AVAILABLE'), (8, 'C8', 'PREMIUM', 'AVAILABLE'),
-- Row D (Premium)
(8, 'D1', 'PREMIUM', 'AVAILABLE'), (8, 'D2', 'PREMIUM', 'AVAILABLE'), (8, 'D3', 'PREMIUM', 'AVAILABLE'), (8, 'D4', 'PREMIUM', 'AVAILABLE'),
(8, 'D5', 'PREMIUM', 'AVAILABLE'), (8, 'D6', 'PREMIUM', 'AVAILABLE'), (8, 'D7', 'PREMIUM', 'AVAILABLE'), (8, 'D8', 'PREMIUM', 'AVAILABLE'),
-- Row E (VIP)
(8, 'E1', 'VIP', 'AVAILABLE'), (8, 'E2', 'VIP', 'AVAILABLE'), (8, 'E3', 'VIP', 'AVAILABLE'), (8, 'E4', 'VIP', 'AVAILABLE'),
(8, 'E5', 'VIP', 'AVAILABLE'), (8, 'E6', 'VIP', 'AVAILABLE'), (8, 'E7', 'VIP', 'AVAILABLE'), (8, 'E8', 'VIP', 'AVAILABLE');
