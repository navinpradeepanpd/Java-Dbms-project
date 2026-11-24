# CineBook: Java & JDBC Cinema Ticket Booking System

A robust, console-based Movie Ticket Booking System built with **Core Java, JDBC, and MySQL**, designed specifically for college semester projects and viva defense.

---

## 📌 Project Overview

**CineBook** is an enterprise-grade yet clean and understandable cinema ticketing application. It demonstrates core Object-Oriented Programming (OOP) principles, clean modular architecture, defensive input handling, and ACID-compliant relational database transaction management using native JDBC.

### Key Highlights
- **5 × 8 Seat Matrix (40 seats per show)**:
  - Rows **A & B** = `REGULAR` (Rs. 150)
  - Rows **C & D** = `PREMIUM` (Rs. 250)
  - Row **E** = `VIP` (Rs. 400)
- **Live Seat Map**: Dynamic `O` (Available) and `X` (Booked) console layout retrieved from MySQL per show.
- **Atomic Multi-Seat Booking**: Supports booking multiple seats in a single database transaction with rollback on collision.
- **Discounts & Coupons**: Method overloading with coupon `CINE10` offering a flat 10% discount.
- **Cancellations & Refunds**: Atomic cancellation releasing booked seats and issuing an 80% refund audit record.
- **Defensive Console UI**: Never crashes on malformed inputs, out-of-range IDs, invalid seat codes (e.g. `Z9`), or letters in numeric prompts.

---

## 🎓 Syllabus Concepts Demonstrated

| Concept | Implementation Location | Viva Defense Explanation |
|---|---|---|
| **Encapsulation & POJOs** | `com.cinebook.model.*` | Private fields with public getters, setters, and constructors. |
| **Enums with Fields** | `com.cinebook.enums.SeatType` | Encapsulates row mapping and base pricing (`150`, `250`, `400`) rather than hardcoding numbers in business logic. |
| **Interfaces & Abstraction** | `com.cinebook.interfaces.Bookable` | Declares business contracts `bookSeats()` and `cancelBooking()`, decoupling API contract from service implementation. |
| **Method Overloading** | `BookingService.java` | Overloaded calculation: `calculateTotal(seats)` vs `calculateTotal(seats, couponCode)`. |
| **Custom Exceptions** | `com.cinebook.exception.*` | Domain-specific checked exceptions (`SeatNotAvailableException`, `InvalidSeatException`, `BookingNotFoundException`, `InvalidCouponException`). |
| **Collections Framework** | Across all layers | `List`, `ArrayList`, `Map`, `HashMap`, `Set`, `HashSet`. |
| **Stream API & Lambdas** | `BookingService`, `CineBookApp` | Used for calculating subtotals, sorting movie titles, and filtering seat availability stats without cumbersome loops. |
| **JDBC Transactions** | `BookingService.java` | Explicit transaction demarcation (`conn.setAutoCommit(false)`, `conn.commit()`, `conn.rollback()`) for ACID compliance. |
| **Connection Factory**| `DatabaseConnection.java` | Centralized JDBC Connection Factory utility managing connection lifecycle and auto-schema execution. |
| **PreparedStatement** | `com.cinebook.dao.*` | Parameterized queries prevent SQL injection and enhance query performance. |

---

## 🗄️ Database Architecture

### Entity-Relationship (ER) Overview
```
 [ MOVIE ] 1 ───────────< M [ MOVIE_SHOW ] 1 ───────────< M [ SEAT ]
                                    │                           │
                                    │ 1                         │ M
                                    ▼                           ▼
[ CUSTOMER ] 1 ────────< M [ BOOKING ] 1 ────< M [ BOOKING_SEAT ]
                                    │
                                    │ 1
                                    ▼ M
                               [ PAYMENT ]
```

### Table Structure
1. **`movie`**: Stores movie catalog (`movie_id`, `movie_name`, `duration`, `language`, `genre`).
2. **`movie_show`**: Scheduled screenings (`show_id`, `movie_id`, `show_date`, `show_time`, `screen_name`). *(Named `movie_show` to avoid MySQL's reserved keyword `SHOW`)*.
3. **`seat`**: 40 distinct seat records per show with unique constraint on `(show_id, seat_code)`.
4. **`customer`**: Patron directory indexed by unique `phone`.
5. **`booking`**: Header record for transactions (`subtotal`, `discount_amount`, `total_amount`, `coupon_code`, `status`).
6. **`booking_seat`**: Junction table mapping booked seats to booking transactions.
7. **`payment`**: Transaction audit logs (`amount`, `payment_type`, `payment_status`, `payment_time`).

---

## 🚀 Getting Started

### Prerequisites
- **Java**: JDK 17 or higher
- **MySQL Server**: 8.0 or higher running on `localhost:3306`

### Database Configuration
Connection details are located in [`src/main/resources/db.properties`](src/main/resources/db.properties):
```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/cinebook_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=root
```
*(CineBook automatically executes `schema.sql` and `data.sql` on startup if tables are not initialized)*.

### Running the Application

#### Option A: Launch the Web Application (Primary Interface)
Double-click or run from command prompt:
```cmd
run-web.bat
```
*(Or via Maven: `.\mvn.cmd spring-boot:run`)*.

Then open your browser at:
👉 **`http://localhost:8080`**

#### Option B: Launch the Console Application (CMD Interface)
Double-click or run from command prompt:
```cmd
run.bat
```
*(Or via Maven: `.\mvn.cmd exec:java`)*.

### Running Automated Test Suite
To verify end-to-end booking, collision handling, coupons, deterministic sorting, and refunds:
```cmd
test.bat
```

---

## 💻 Console Application Interface

```text
==================== CINEBOOK MAIN MENU ====================
  1. View Movies
  2. View Shows
  3. View Seat Map
  4. Book Tickets
  5. View Booking Details
  6. View Booking History
  7. Cancel Booking
  8. Exit
============================================================
```

### 5 × 8 Seat Map Layout
```text
============= SEAT MAP FOR SHOW #1 =============
                 [ SCREEN THIS WAY ]                 
-----------------------------------------------------
      1   2   3   4   5   6   7   8
A     O   O   O   O   O   O   O   O    [Regular - Rs.150]
B     O   O   O   O   O   O   O   O    [Regular - Rs.150]
C     O   O   O   O   O   O   O   O    [Premium - Rs.250]
D     O   O   O   O   O   O   O   O    [Premium - Rs.250]
E     O   O   O   O   O   O   O   O    [VIP     - Rs.400]
-----------------------------------------------------
Legend:  O = Available  |  X = Booked
Summary: 40 Available  |  0 Booked  |  Total: 40
=====================================================
```

---

## 🎤 Viva Defense Q&A Cheatsheet

### 1. Why do you use database transactions (`setAutoCommit(false)`) during booking?
> **Answer**: Booking involves modifying multiple tables: checking seat availability, updating seats to `BOOKED`, creating a `booking` row, inserting `booking_seat` mappings, and recording the `payment`. If any step fails (e.g. power loss or seat collision), calling `conn.rollback()` ensures the database returns to its previous valid state without leaving orphaned bookings or permanently blocked seats.

### 2. How did you prevent SQL Injection?
> **Answer**: We strictly used `PreparedStatement` with parameterized placeholders (`?`) across all DAO classes. User input is treated strictly as data rather than executable SQL syntax.

### 3. Why is pricing defined inside the `SeatType` enum?
> **Answer**: By defining prices (`150`, `250`, `400`) and row mappings inside the `SeatType` enum, pricing logic is encapsulated in a single source of truth. If prices change in the future, we only update the enum rather than refactoring multiple SQL queries and service methods.

### 4. What is the role of the `Bookable` interface?
> **Answer**: The `Bookable` interface defines a formal business contract declaring `bookSeats()` and `cancelBooking()`. This demonstrates the OOP principle of Abstraction and Polymorphism, allowing the presentation layer to rely on the interface contract rather than direct database operations.

### 5. Why is the table named `movie_show` instead of `show`?
> **Answer**: In SQL (and MySQL specifically), `SHOW` is a reserved command keyword (e.g., `SHOW TABLES;`). Using `movie_show` prevents query parser conflicts and ensures portability without needing backtick escaping everywhere.

### 6. Is `DatabaseConnection` a Singleton? Why return a new Connection each time?
> **Answer**: `DatabaseConnection` is a Connection Factory / Utility class. It returns a new `Connection` via `DriverManager.getConnection(...)` per transaction/query. If the `Connection` object itself were a shared static Singleton, concurrent threads calling `setAutoCommit(false)` or `commit()` would interleave and corrupt each other's database transactions.

### 7. How does the system ensure double-cancellation cannot occur?
> **Answer**: In `BookingDAO`, `updateBookingStatus` executes `UPDATE booking SET status = ? WHERE booking_id = ? AND status = 'CONFIRMED'`. If an already-cancelled booking is processed, zero rows are affected and an exception is thrown, preventing double refunds.

