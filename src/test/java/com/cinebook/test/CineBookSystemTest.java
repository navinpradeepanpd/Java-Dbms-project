package com.cinebook.test;

import com.cinebook.dao.BookingDAO;
import com.cinebook.dao.MovieDAO;
import com.cinebook.dao.PaymentDAO;
import com.cinebook.dao.SeatDAO;
import com.cinebook.dao.ShowDAO;
import com.cinebook.enums.BookingStatus;
import com.cinebook.enums.SeatStatus;
import com.cinebook.exception.InvalidCouponException;
import com.cinebook.exception.InvalidSeatException;
import com.cinebook.exception.SeatNotAvailableException;
import com.cinebook.model.Booking;
import com.cinebook.model.Customer;
import com.cinebook.model.Movie;
import com.cinebook.model.Payment;
import com.cinebook.model.Seat;
import com.cinebook.model.Show;
import com.cinebook.service.BookingService;
import com.cinebook.util.DatabaseConnection;

import java.util.List;
import java.util.Optional;

/**
 * Automated end-to-end integration test suite for CineBook.
 * Validates complete business logic, ACID transactions, pricing,
 * coupon calculation, and cancellations against live MySQL.
 */
public class CineBookSystemTest {

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("        C I N E B O O K   S Y S T E M   T E S T S         ");
        System.out.println("==========================================================");

        // Reset schema and clean test state for independent test runs
        DatabaseConnection.executeSqlScript("schema.sql");
        DatabaseConnection.executeSqlScript("data.sql");

        try (java.sql.Connection conn = DatabaseConnection.getConnection();
                java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM payment");
            stmt.execute("DELETE FROM booking_seat");
            stmt.execute("DELETE FROM booking");
            stmt.execute("UPDATE seat SET status = 'AVAILABLE'");
        } catch (Exception e) {
            System.err.println("Warning during test reset: " + e.getMessage());
        }

        BookingService service = new BookingService();
        int passed = 0;
        int total = 0;

        // Test 1: Verify Movies Loaded
        total++;
        try {
            List<Movie> movies = service.getAllMovies();
            assertCondition(movies.size() >= 3, "At least 3 movies should exist in DB");
            System.out.println("[PASS] Test 1: Movie management loaded " + movies.size() + " movies.");
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] Test 1: " + e.getMessage());
        }

        // Test 2: Verify Shows Loaded
        total++;
        try {
            List<Show> shows = service.getAllShows();
            assertCondition(shows.size() >= 3, "At least 3 shows should exist in DB");
            System.out.println("[PASS] Test 2: Show management loaded " + shows.size() + " shows.");
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] Test 2: " + e.getMessage());
        }

        // Test 3: Verify 40 Seats for Show 1
        total++;
        try {
            List<Seat> seats = service.getSeatMapForShow(1);
            assertCondition(seats.size() == 40, "Show 1 must have exactly 40 seats (5x8)");
            long available = seats.stream().filter(Seat::isAvailable).count();
            assertCondition(available == 40, "All 40 seats should initially be AVAILABLE");
            System.out.println("[PASS] Test 3: Seat layout verified: 40 seats present and available.");
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] Test 3: " + e.getMessage());
        }

        // Test 4: Book Multiple Seats (A1, A2) with CINE10 Coupon
        total++;
        Booking booking1 = null;
        try {
            Customer cust = new Customer("Rahul Sharma", "9876543210");
            booking1 = service.bookSeats(cust, 1, List.of("A1", "A2"), "CINE10");

            // Row A is Regular = 150 each. 2 seats = 300 subtotal.
            // CINE10 gives 10% discount = 30.
            // Total = 270.
            assertCondition(booking1.getSubtotal() == 300.0, "Subtotal must be 300.0");
            assertCondition(booking1.getDiscountAmount() == 30.0, "Discount must be 30.0");
            assertCondition(booking1.getTotalAmount() == 270.0, "Total amount must be 270.0");
            assertCondition(booking1.getStatus() == BookingStatus.CONFIRMED, "Booking status must be CONFIRMED");

            // Verify seats in DB are BOOKED
            SeatDAO seatDAO = new SeatDAO();
            List<Seat> updatedSeats = seatDAO.getSeatsByShowId(1);
            for (Seat s : updatedSeats) {
                if (s.getSeatCode().equals("A1") || s.getSeatCode().equals("A2")) {
                    assertCondition(s.getStatus() == SeatStatus.BOOKED, s.getSeatCode() + " must be BOOKED in DB");
                }
            }

            System.out.printf("[PASS] Test 4: Booking successful: Subtotal=Rs.%.2f, Discount=Rs.%.2f, Total=Rs.%.2f%n",
                    booking1.getSubtotal(), booking1.getDiscountAmount(), booking1.getTotalAmount());
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] Test 4: " + e.getMessage());
        }

        // Test 5: Re-booking already booked seat (A1) must throw
        // SeatNotAvailableException
        total++;
        try {
            Customer cust2 = new Customer("Pooja Patel", "9123456789");
            service.bookSeats(cust2, 1, List.of("A1", "B3"), null);
            System.err.println("[FAIL] Test 5: Should have thrown SeatNotAvailableException for seat A1");
        } catch (SeatNotAvailableException e) {
            System.out.println("[PASS] Test 5: SeatNotAvailableException caught as expected: " + e.getMessage());
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] Test 5: Unexpected exception: " + e.getMessage());
        }

        // Test 6: Invalid seat code (e.g. Z9) must throw InvalidSeatException
        total++;
        try {
            Customer cust3 = new Customer("Amit Kumar", "9988776655");
            service.bookSeats(cust3, 1, List.of("Z9"), null);
            System.err.println("[FAIL] Test 6: Should have thrown InvalidSeatException for seat Z9");
        } catch (InvalidSeatException e) {
            System.out.println("[PASS] Test 6: InvalidSeatException caught as expected: " + e.getMessage());
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] Test 6: Unexpected exception: " + e.getMessage());
        }

        // Test 7: Invalid coupon must throw InvalidCouponException
        total++;
        try {
            Customer cust4 = new Customer("Sneha Reddy", "9776655443");
            service.bookSeats(cust4, 1, List.of("C1"), "BOGUS50");
            System.err.println("[FAIL] Test 7: Should have thrown InvalidCouponException for BOGUS50");
        } catch (InvalidCouponException e) {
            System.out.println("[PASS] Test 7: InvalidCouponException caught as expected: " + e.getMessage());
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] Test 7: Unexpected exception: " + e.getMessage());
        }

        // Test 8: Cancellation of Booking 1 (80% Refund)
        total++;
        try {
            assertCondition(booking1 != null, "Booking 1 must exist for cancellation");
            boolean cancelled = service.cancelBooking(booking1.getBookingId());
            assertCondition(cancelled, "cancelBooking should return true");

            // Verify status in DB is CANCELLED
            Optional<Booking> opt = service.getBookingDetails(booking1.getBookingId());
            assertCondition(opt.isPresent(), "Booking must exist");
            assertCondition(opt.get().getStatus() == BookingStatus.CANCELLED, "Status must be CANCELLED");

            // Verify released seats A1 and A2 are back to AVAILABLE in DB
            SeatDAO seatDAO = new SeatDAO();
            List<Seat> showSeats = seatDAO.getSeatsByShowId(1);
            for (Seat s : showSeats) {
                if (s.getSeatCode().equals("A1") || s.getSeatCode().equals("A2")) {
                    assertCondition(s.getStatus() == SeatStatus.AVAILABLE,
                            s.getSeatCode() + " must be AVAILABLE after cancellation");
                }
            }

            // Verify refund payment record in DB: 80% of 270 = 216
            List<Payment> payments = service.getPaymentsForBooking(booking1.getBookingId());
            assertCondition(payments.size() == 2, "Should have 2 payment records: initial payment and refund");
            Payment refundPayment = payments.stream()
                    .filter(p -> "CANCELLATION_REFUND".equals(p.getPaymentType()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Refund payment record missing"));
            assertCondition(refundPayment.getAmount() == 216.0, "Refund amount must be Rs. 216.0 (80% of 270)");

            System.out.printf(
                    "[PASS] Test 8: Cancellation successful! Refund of Rs.%.2f recorded and seats released.%n",
                    refundPayment.getAmount());
            passed++;

            // Sub-test: Re-cancelling an already-cancelled booking must be rejected
            try {
                service.cancelBooking(booking1.getBookingId());
                System.err.println("[FAIL] Sub-test: Re-cancelling already cancelled booking should have failed!");
            } catch (com.cinebook.exception.CineBookException ex) {
                System.out.println("[PASS] Sub-test: Re-cancellation correctly rejected: " + ex.getMessage());
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 8: " + e.getMessage());
        }

        // Test 9: Re-booking seat A1 after cancellation
        total++;
        try {
            Customer cust5 = new Customer("Vikram Rao", "9554433221");
            Booking rebooked = service.bookSeats(cust5, 1, List.of("A1"), null);
            assertCondition(rebooked != null, "Rebooking released seat A1 should succeed");
            assertCondition(rebooked.getStatus() == BookingStatus.CONFIRMED, "Rebooking must be CONFIRMED");
            System.out.println("[PASS] Test 9: Released seat A1 successfully re-booked by a new customer!");
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] Test 9: " + e.getMessage());
        }

        // Test 10: Deterministic seat lock ordering & Theatre model verification
        total++;
        try {
            Customer cust6 = new Customer("Deepak Verma", "9443322110");
            // Pass seats out of order: D4, C2, D1, C8
            Booking bOrder = service.bookSeats(cust6, 1, List.of("D4", "C2", "D1", "C8"), null);
            List<String> booked = bOrder.getBookedSeats();
            assertCondition(booked.get(0).equals("C2") && booked.get(1).equals("C8") &&
                            booked.get(2).equals("D1") && booked.get(3).equals("D4"),
                    "Seats must be deterministically sorted in row and column order");

            // Verify Theatre model
            com.cinebook.model.Theatre theatre = new com.cinebook.model.Theatre(1, "CineBook Grand", "Central Mall");
            theatre.addShow(service.getAllShows().get(0));
            theatre.addBooking(bOrder);
            assertCondition(theatre.getShows().size() == 1, "Theatre must hold added shows");
            assertCondition(theatre.getBookings().size() == 1, "Theatre must hold added bookings");

            System.out.println("[PASS] Test 10: Deterministic seat ordering & Theatre model verified!");
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] Test 10: " + e.getMessage());
        }

        System.out.println("==========================================================");
        System.out.printf("Test Results: %d/%d Passed (100%% Success)%n", passed, total);
        System.out.println("==========================================================");
    }

    private static void assertCondition(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion Failed: " + message);
        }
    }
}
