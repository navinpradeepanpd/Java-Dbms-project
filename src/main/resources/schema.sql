-- ===================================================================
-- CineBook: Movie Ticket Booking System - Database Schema
-- Compatible with MySQL 8.0+
-- ===================================================================

CREATE DATABASE IF NOT EXISTS cinebook_db;
USE cinebook_db;

-- 1. MOVIE TABLE
CREATE TABLE IF NOT EXISTS movie (
    movie_id INT AUTO_INCREMENT PRIMARY KEY,
    movie_name VARCHAR(100) NOT NULL,
    duration INT NOT NULL,                     -- in minutes
    language VARCHAR(30) NOT NULL,
    genre VARCHAR(50) NOT NULL
) ENGINE=InnoDB;

-- 2. MOVIE_SHOW TABLE (named movie_show to avoid MySQL reserved keyword 'SHOW')
CREATE TABLE IF NOT EXISTS movie_show (
    show_id INT AUTO_INCREMENT PRIMARY KEY,
    movie_id INT NOT NULL,
    show_date DATE NOT NULL,
    show_time TIME NOT NULL,
    screen_name VARCHAR(20) DEFAULT 'Screen 1',
    FOREIGN KEY (movie_id) REFERENCES movie(movie_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 3. SEAT TABLE (Each show maintains independent 40-seat inventory: 5 rows x 8 columns)
CREATE TABLE IF NOT EXISTS seat (
    seat_id INT AUTO_INCREMENT PRIMARY KEY,
    show_id INT NOT NULL,
    seat_code VARCHAR(5) NOT NULL,              -- 'A1' through 'E8'
    seat_type VARCHAR(20) NOT NULL,             -- 'REGULAR', 'PREMIUM', 'VIP'
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE', -- 'AVAILABLE', 'BOOKED'
    FOREIGN KEY (show_id) REFERENCES movie_show(show_id) ON DELETE CASCADE,
    UNIQUE KEY uq_show_seat (show_id, seat_code)
) ENGINE=InnoDB;

-- 4. CUSTOMER TABLE
CREATE TABLE IF NOT EXISTS customer (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- 5. BOOKING TABLE
CREATE TABLE IF NOT EXISTS booking (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    show_id INT NOT NULL,
    booking_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    subtotal DECIMAL(10, 2) NOT NULL,
    discount_amount DECIMAL(10, 2) DEFAULT 0.00,
    total_amount DECIMAL(10, 2) NOT NULL,
    coupon_code VARCHAR(20) DEFAULT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED', -- 'CONFIRMED', 'CANCELLED'
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    FOREIGN KEY (show_id) REFERENCES movie_show(show_id)
) ENGINE=InnoDB;

-- 6. BOOKING_SEAT TABLE (Junction table linking bookings to reserved seats)
CREATE TABLE IF NOT EXISTS booking_seat (
    booking_seat_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    seat_id INT NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES booking(booking_id) ON DELETE CASCADE,
    FOREIGN KEY (seat_id) REFERENCES seat(seat_id)
) ENGINE=InnoDB;

-- 7. PAYMENT TABLE (Audit records for bookings and refunds)
CREATE TABLE IF NOT EXISTS payment (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_type VARCHAR(30) NOT NULL,          -- 'BOOKING_PAYMENT', 'CANCELLATION_REFUND'
    payment_status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS', -- 'SUCCESS', 'FAILED'
    payment_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES booking(booking_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 8. USERS TABLE (Spring Security Customer & Admin Authentication)
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,       -- mobile phone or email/username
    password VARCHAR(255) NOT NULL,             -- BCrypt hashed password
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER', -- 'CUSTOMER', 'ADMIN'
    customer_id INT DEFAULT NULL,               -- linked customer record if customer
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE SET NULL
) ENGINE=InnoDB;


