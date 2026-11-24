package com.cinebook.dao;

import com.cinebook.model.Payment;
import com.cinebook.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Payment entity.
 */
public class PaymentDAO {

    /**
     * Records a payment or refund entry within an active transaction.
     */
    public int createPayment(Connection conn, Payment payment) throws SQLException {
        String sql = """
            INSERT INTO payment (booking_id, amount, payment_type, payment_status, payment_time)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, payment.getBookingId());
            stmt.setDouble(2, payment.getAmount());
            stmt.setString(3, payment.getPaymentType());
            stmt.setString(4, payment.getPaymentStatus());
            stmt.setTimestamp(5, Timestamp.valueOf(payment.getPaymentTime()));

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    payment.setPaymentId(id);
                    return id;
                } else {
                    throw new SQLException("Creating payment failed, no generated ID obtained.");
                }
            }
        }
    }

    /**
     * Retrieves all payments associated with a booking ID.
     */
    public List<Payment> getPaymentsByBookingId(int bookingId) throws SQLException {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT payment_id, booking_id, amount, payment_type, payment_status, payment_time " +
                     "FROM payment WHERE booking_id = ? ORDER BY payment_time";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(new Payment(
                            rs.getInt("payment_id"),
                            rs.getInt("booking_id"),
                            rs.getDouble("amount"),
                            rs.getString("payment_type"),
                            rs.getString("payment_status"),
                            rs.getTimestamp("payment_time").toLocalDateTime()
                    ));
                }
            }
        }
        return payments;
    }
}

