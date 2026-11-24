package com.cinebook.service;

import com.cinebook.dao.BookingDAO;
import com.cinebook.dao.CustomerDAO;
import com.cinebook.dao.MovieDAO;
import com.cinebook.dao.PaymentDAO;
import com.cinebook.dao.SeatDAO;
import com.cinebook.dao.ShowDAO;
import com.cinebook.enums.BookingStatus;
import com.cinebook.enums.SeatStatus;
import com.cinebook.exception.BookingNotFoundException;
import com.cinebook.exception.CineBookException;
import com.cinebook.exception.InvalidCouponException;
import com.cinebook.exception.InvalidSeatException;
import com.cinebook.exception.SeatNotAvailableException;
import com.cinebook.interfaces.Bookable;
import com.cinebook.model.Booking;
import com.cinebook.model.Customer;
import com.cinebook.model.Movie;
import com.cinebook.model.Payment;
import com.cinebook.model.Seat;
import com.cinebook.model.Show;
import com.cinebook.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service Layer implementation for CineBook.
 * Implements the Bookable interface and coordinates ACID transactions across
 * customers, shows, seats, bookings, and payments.
 */
public class BookingService implements Bookable {

    public static final String COUPON_CINE10 = "CINE10";
    public static final double CINE10_DISCOUNT_PERCENT = 0.10; // 10%
    public static final double CANCELLATION_REFUND_PERCENT = 0.80; // 80%

    // Regular expression for valid seat codes: Rows A-E and Columns 1-8
    private static final Pattern SEAT_CODE_PATTERN = Pattern.compile("^[A-E][1-8]$", Pattern.CASE_INSENSITIVE);

    private final MovieDAO movieDAO;
    private final ShowDAO showDAO;
    private final SeatDAO seatDAO;
    private final CustomerDAO customerDAO;
    private final BookingDAO bookingDAO;
    private final PaymentDAO paymentDAO;

    public BookingService() {
        this.movieDAO = new MovieDAO();
        this.showDAO = new ShowDAO();
        this.seatDAO = new SeatDAO();
        this.customerDAO = new CustomerDAO();
        this.bookingDAO = new BookingDAO();
        this.paymentDAO = new PaymentDAO();
    }

    // =========================================================================
    // METHOD OVERLOADING FOR TOTAL CALCULATION (Viva Requirement)
    // =========================================================================

    /**
     * Overloaded calculation: Calculates standard total without coupon discount.
     */
    public double calculateTotal(List<Seat> seats) {
        return calculateSubtotal(seats);
    }

    /**
     * Overloaded calculation: Calculates discounted total given a coupon code.
     */
    public double calculateTotal(List<Seat> seats, String couponCode) throws InvalidCouponException {
        double subtotal = calculateSubtotal(seats);
        double discount = calculateDiscount(subtotal, couponCode);
        return Math.max(0.0, subtotal - discount);
    }

    /**
     * Calculates the subtotal using Stream API mapping each seat to its SeatType price.
     */
    public double calculateSubtotal(List<Seat> seats) {
        if (seats == null || seats.isEmpty()) return 0.0;
        return seats.stream()
                .mapToDouble(seat -> seat.getSeatType().getPrice())
                .sum();
    }

    /**
     * Evaluates discount based on coupon code.
     */
    public double calculateDiscount(double subtotal, String couponCode) throws InvalidCouponException {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            return 0.0;
        }

        String cleanedCoupon = couponCode.trim().toUpperCase();
        if (COUPON_CINE10.equals(cleanedCoupon)) {
            return subtotal * CINE10_DISCOUNT_PERCENT;
        } else {
            throw new InvalidCouponException("Invalid coupon code '" + couponCode + "'. Only 'CINE10' is supported.");
        }
    }

    // =========================================================================
    // CORE TRANSACTION: BOOK SEATS (Bookable Interface Implementation)
    // =========================================================================

    @Override
    public Booking bookSeats(Customer customer, int showId, List<String> seatCodes, String couponCode) throws CineBookException {
        // 1. Pre-validation of input
        if (customer == null || customer.getCustomerName() == null || customer.getPhone() == null) {
            throw new CineBookException("Customer details cannot be empty.");
        }
        if (seatCodes == null || seatCodes.isEmpty()) {
            throw new CineBookException("At least one seat must be selected.");
        }

        // Normalize and validate seat codes format (e.g. A1, B4)
        List<String> normalizedCodes = new ArrayList<>();
        Set<String> uniqueCheck = new HashSet<>();
        for (String rawCode : seatCodes) {
            String trimmed = rawCode.trim().toUpperCase();
            if (!SEAT_CODE_PATTERN.matcher(trimmed).matches()) {
                throw new InvalidSeatException("Invalid seat code '" + rawCode + "'. Valid seats are rows A-E and columns 1-8 (e.g., A1, C5).");
            }
            if (!uniqueCheck.add(trimmed)) {
                throw new InvalidSeatException("Duplicate seat code '" + rawCode + "' in request.");
            }
            normalizedCodes.add(trimmed);
        }

        // Sort seat codes into a deterministic order (e.g. A1, A2.. B1, B2..)
        // Ensures consistent lock acquisition order and prevents deadlocks under concurrency
        normalizedCodes.sort(Comparator.comparing((String s) -> s.charAt(0))
                .thenComparingInt(s -> Integer.parseInt(s.substring(1))));

        // Verify show exists
        Show show;
        try {
            Optional<Show> showOpt = showDAO.getShowById(showId);
            if (showOpt.isEmpty()) {
                throw new CineBookException("Show #" + showId + " does not exist.");
            }
            show = showOpt.get();
        } catch (SQLException e) {
            throw new CineBookException("Database error checking show: " + e.getMessage(), e);
        }

        // 2. Begin JDBC Transaction
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // START TRANSACTION

            // Step A: Ensure customer exists in DB
            int customerId = customerDAO.createOrGetCustomer(conn, customer);
            customer.setCustomerId(customerId);

            // Step B: Query and Lock Seats for Update
            List<Seat> seats = seatDAO.getSeatsByCodesForUpdate(conn, showId, normalizedCodes);

            if (seats.size() != normalizedCodes.size()) {
                Set<String> foundCodes = seats.stream().map(Seat::getSeatCode).collect(Collectors.toSet());
                List<String> missing = normalizedCodes.stream()
                        .filter(code -> !foundCodes.contains(code))
                        .collect(Collectors.toList());
                throw new InvalidSeatException("Seats not found for Show #" + showId + ": " + missing);
            }

            // Step C: Verify all seats are AVAILABLE
            List<String> unavailableSeats = seats.stream()
                    .filter(seat -> seat.getStatus() != SeatStatus.AVAILABLE)
                    .map(Seat::getSeatCode)
                    .collect(Collectors.toList());

            if (!unavailableSeats.isEmpty()) {
                throw new SeatNotAvailableException("Cannot complete booking. Seats already booked: " + unavailableSeats);
            }

            // Step D: Pricing calculation
            double subtotal = calculateSubtotal(seats);
            double discount = 0.0;
            String normalizedCoupon = null;
            if (couponCode != null && !couponCode.trim().isEmpty()) {
                discount = calculateDiscount(subtotal, couponCode);
                normalizedCoupon = couponCode.trim().toUpperCase();
            }
            double totalAmount = subtotal - discount;

            // Step E: Update seats status to BOOKED
            List<Integer> seatIds = seats.stream().map(Seat::getSeatId).collect(Collectors.toList());
            seatDAO.updateSeatStatuses(conn, seatIds, SeatStatus.BOOKED);

            // Step F: Create Booking record
            Booking booking = new Booking();
            booking.setCustomerId(customerId);
            booking.setShowId(showId);
            booking.setBookingTime(LocalDateTime.now());
            booking.setSubtotal(subtotal);
            booking.setDiscountAmount(discount);
            booking.setTotalAmount(totalAmount);
            booking.setCouponCode(normalizedCoupon);
            booking.setStatus(BookingStatus.CONFIRMED);

            int bookingId = bookingDAO.createBooking(conn, booking);

            // Step G: Insert booking-seat mapping records
            bookingDAO.linkBookingSeats(conn, bookingId, seatIds);

            // Step H: Create Payment audit record
            Payment payment = new Payment(bookingId, totalAmount, "BOOKING_PAYMENT", "SUCCESS");
            paymentDAO.createPayment(conn, payment);

            // Step I: COMMIT TRANSACTION
            conn.commit();

            // Populate presentation fields
            booking.setBookingId(bookingId);
            booking.setCustomerName(customer.getCustomerName());
            booking.setCustomerPhone(customer.getPhone());
            booking.setMovieTitle(show.getMovieName());
            booking.setShowDate(show.getShowDate());
            booking.setShowTime(show.getShowTime());
            booking.setScreenName(show.getScreenName());
            booking.setBookedSeats(normalizedCodes);

            return booking;

        } catch (CineBookException e) {
            rollbackQuietly(conn);
            throw e;
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new CineBookException("Database transaction failed during booking: " + e.getMessage(), e);
        } finally {
            resetAutoCommitAndClose(conn);
        }
    }

    // =========================================================================
    // CORE TRANSACTION: CANCEL BOOKING (Bookable Interface Implementation)
    // =========================================================================

    @Override
    public boolean cancelBooking(int bookingId) throws CineBookException {
        // Step 1: Look up booking
        Booking booking;
        try {
            Optional<Booking> opt = bookingDAO.getBookingById(bookingId);
            if (opt.isEmpty()) {
                throw new BookingNotFoundException("Booking #" + bookingId + " not found.");
            }
            booking = opt.get();
        } catch (SQLException e) {
            throw new CineBookException("Database error finding booking: " + e.getMessage(), e);
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new CineBookException("Booking #" + bookingId + " has already been cancelled.");
        }

        // Step 2: Begin Cancellation Transaction
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // START TRANSACTION

            // Step A: Mark booking status as CANCELLED
            boolean updated = bookingDAO.updateBookingStatus(conn, bookingId, BookingStatus.CANCELLED);
            if (!updated) {
                throw new CineBookException("Failed to update status for Booking #" + bookingId);
            }

            // Step B: Get linked seat IDs and release them back to AVAILABLE
            List<Integer> seatIds = bookingDAO.getSeatIdsForBooking(conn, bookingId);
            seatDAO.updateSeatStatuses(conn, seatIds, SeatStatus.AVAILABLE);

            // Step C: Calculate 80% refund of the net total paid
            double refundAmount = booking.getTotalAmount() * CANCELLATION_REFUND_PERCENT;

            // Step D: Insert refund payment audit record
            Payment refundPayment = new Payment(bookingId, refundAmount, "CANCELLATION_REFUND", "SUCCESS");
            paymentDAO.createPayment(conn, refundPayment);

            // Step E: COMMIT TRANSACTION
            conn.commit();
            return true;

        } catch (CineBookException e) {
            rollbackQuietly(conn);
            throw e;
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new CineBookException("Database transaction failed during cancellation: " + e.getMessage(), e);
        } finally {
            resetAutoCommitAndClose(conn);
        }
    }

    // =========================================================================
    // CONVENIENCE & LOOKUP METHODS FOR CLI
    // =========================================================================

    public List<Movie> getAllMovies() throws SQLException {
        return movieDAO.getAllMovies();
    }

    public List<Show> getAllShows() throws SQLException {
        return showDAO.getAllShows();
    }

    public List<Show> getShowsByMovie(int movieId) throws SQLException {
        return showDAO.getShowsByMovieId(movieId);
    }

    public List<Seat> getSeatMapForShow(int showId) throws SQLException {
        return seatDAO.getSeatsByShowId(showId);
    }

    public Optional<Booking> getBookingDetails(int bookingId) throws SQLException {
        return bookingDAO.getBookingById(bookingId);
    }

    public List<Booking> getCustomerBookings(String phone) throws SQLException {
        return bookingDAO.getBookingsByCustomerPhone(phone);
    }

    public List<Booking> getCustomerBookings(String phone, boolean confirmedOnly) throws SQLException {
        return bookingDAO.getBookingsByCustomerPhone(phone, confirmedOnly);
    }

    public List<Payment> getPaymentsForBooking(int bookingId) throws SQLException {
        return paymentDAO.getPaymentsByBookingId(bookingId);
    }

    // =========================================================================
    // TRANSACTION HELPER UTILITIES
    // =========================================================================

    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
        }
    }

    private void resetAutoCommitAndClose(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
            DatabaseConnection.closeQuietly(conn);
        }
    }
}

