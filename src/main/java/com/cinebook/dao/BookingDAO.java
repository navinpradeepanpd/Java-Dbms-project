package com.cinebook.dao;

import com.cinebook.enums.BookingStatus;
import com.cinebook.model.Booking;
import com.cinebook.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Booking entity and booking_seat junction records.
 */
public class BookingDAO {

    /**
     * Inserts a new booking record inside an active transaction.
     */
    public int createBooking(Connection conn, Booking booking) throws SQLException {
        String sql = """
            INSERT INTO booking (customer_id, show_id, subtotal, discount_amount, total_amount, coupon_code, status)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, booking.getCustomerId());
            stmt.setInt(2, booking.getShowId());
            stmt.setDouble(3, booking.getSubtotal());
            stmt.setDouble(4, booking.getDiscountAmount());
            stmt.setDouble(5, booking.getTotalAmount());
            stmt.setString(6, booking.getCouponCode());
            stmt.setString(7, booking.getStatus().name());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    booking.setBookingId(id);
                    return id;
                } else {
                    throw new SQLException("Creating booking failed, no generated ID obtained.");
                }
            }
        }
    }

    /**
     * Links reserved seats to a booking in the junction table booking_seat within a transaction.
     */
    public void linkBookingSeats(Connection conn, int bookingId, List<Integer> seatIds) throws SQLException {
        if (seatIds == null || seatIds.isEmpty()) return;

        String sql = "INSERT INTO booking_seat (booking_id, seat_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int seatId : seatIds) {
                stmt.setInt(1, bookingId);
                stmt.setInt(2, seatId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    /**
     * Retrieves all seat IDs linked to a specific booking ID.
     */
    public List<Integer> getSeatIdsForBooking(Connection conn, int bookingId) throws SQLException {
        List<Integer> seatIds = new ArrayList<>();
        String sql = "SELECT seat_id FROM booking_seat WHERE booking_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seatIds.add(rs.getInt("seat_id"));
                }
            }
        }
        return seatIds;
    }

    /**
     * Updates booking status (e.g. to CANCELLED) inside a transaction.
     * Ensures atomic state transition: only an active 'CONFIRMED' booking can be cancelled.
     */
    public boolean updateBookingStatus(Connection conn, int bookingId, BookingStatus status) throws SQLException {
        String sql = "UPDATE booking SET status = ? WHERE booking_id = ? AND status = 'CONFIRMED'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, bookingId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves a complete booking record by its ID, including movie, show, customer, and seat details.
     */
    public Optional<Booking> getBookingById(int bookingId) throws SQLException {
        String sql = """
            SELECT b.booking_id, b.customer_id, b.show_id, b.booking_time,
                   b.subtotal, b.discount_amount, b.total_amount, b.coupon_code, b.status,
                   c.customer_name, c.phone,
                   m.movie_name, ms.show_date, ms.show_time, ms.screen_name
            FROM booking b
            JOIN customer c ON b.customer_id = c.customer_id
            JOIN movie_show ms ON b.show_id = ms.show_id
            JOIN movie m ON ms.movie_id = m.movie_id
            WHERE b.booking_id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Booking booking = mapResultSetToBooking(rs);
                    // Populate booked seat codes
                    SeatDAO seatDAO = new SeatDAO();
                    booking.setBookedSeats(seatDAO.getSeatCodesByBookingId(bookingId));
                    return Optional.of(booking);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves all active CONFIRMED bookings for a customer identified by phone number.
     * Cancelled bookings are excluded from customer-facing lookup results.
     */
    public List<Booking> getBookingsByCustomerPhone(String phone) throws SQLException {
        return getBookingsByCustomerPhone(phone, true);
    }

    /**
     * Retrieves bookings for a customer identified by phone number.
     * If confirmedOnly is true, returns only CONFIRMED bookings.
     * If confirmedOnly is false, returns all bookings (including CANCELLED).
     */
    public List<Booking> getBookingsByCustomerPhone(String phone, boolean confirmedOnly) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = """
            SELECT b.booking_id, b.customer_id, b.show_id, b.booking_time,
                   b.subtotal, b.discount_amount, b.total_amount, b.coupon_code, b.status,
                   c.customer_name, c.phone,
                   m.movie_name, ms.show_date, ms.show_time, ms.screen_name
            FROM booking b
            JOIN customer c ON b.customer_id = c.customer_id
            JOIN movie_show ms ON b.show_id = ms.show_id
            JOIN movie m ON ms.movie_id = m.movie_id
            WHERE c.phone = ?""" + (confirmedOnly ? " AND b.status = 'CONFIRMED'" : "") + """
            ORDER BY b.booking_time DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, phone.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                SeatDAO seatDAO = new SeatDAO();
                while (rs.next()) {
                    Booking b = mapResultSetToBooking(rs);
                    b.setBookedSeats(seatDAO.getSeatCodesByBookingId(b.getBookingId()));
                    bookings.add(b);
                }
            }
        }
        return bookings;
    }

    /**
     * Retrieves all bookings for Admin management overview.
     */
    public List<Booking> getAllBookings() throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = """
            SELECT b.booking_id, b.customer_id, b.show_id, b.booking_time,
                   b.subtotal, b.discount_amount, b.total_amount, b.coupon_code, b.status,
                   c.customer_name, c.phone,
                   m.movie_name, ms.show_date, ms.show_time, ms.screen_name
            FROM booking b
            JOIN customer c ON b.customer_id = c.customer_id
            JOIN movie_show ms ON b.show_id = ms.show_id
            JOIN movie m ON ms.movie_id = m.movie_id
            ORDER BY b.booking_time DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            SeatDAO seatDAO = new SeatDAO();
            while (rs.next()) {
                Booking b = mapResultSetToBooking(rs);
                b.setBookedSeats(seatDAO.getSeatCodesByBookingId(b.getBookingId()));
                bookings.add(b);
            }
        }
        return bookings;
    }

    private Booking mapResultSetToBooking(ResultSet rs) throws SQLException {
        Booking booking = new Booking(
                rs.getInt("booking_id"),
                rs.getInt("customer_id"),
                rs.getInt("show_id"),
                rs.getTimestamp("booking_time").toLocalDateTime(),
                rs.getDouble("subtotal"),
                rs.getDouble("discount_amount"),
                rs.getDouble("total_amount"),
                rs.getString("coupon_code"),
                BookingStatus.fromString(rs.getString("status"))
        );
        booking.setCustomerName(rs.getString("customer_name"));
        booking.setCustomerPhone(rs.getString("phone"));
        booking.setMovieTitle(rs.getString("movie_name"));
        booking.setShowDate(rs.getDate("show_date").toLocalDate());
        booking.setShowTime(rs.getTime("show_time").toLocalTime());
        booking.setScreenName(rs.getString("screen_name"));
        return booking;
    }
}

