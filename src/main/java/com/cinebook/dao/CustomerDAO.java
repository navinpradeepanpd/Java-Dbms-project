package com.cinebook.dao;

import com.cinebook.model.Customer;
import com.cinebook.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * Data Access Object for Customer entity.
 */
public class CustomerDAO {

    public Optional<Customer> findByPhone(String phone) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return findByPhone(conn, phone);
        }
    }

    public Optional<Customer> findByPhone(Connection conn, String phone) throws SQLException {
        String sql = "SELECT customer_id, customer_name, phone FROM customer WHERE phone = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Customer> findById(int customerId) throws SQLException {
        String sql = "SELECT customer_id, customer_name, phone FROM customer WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Looks up an existing customer by phone number, or inserts a new one inside an active transaction.
     * Guarantees customer persistence and assigns customerId.
     */
    public int createOrGetCustomer(Connection conn, Customer customer) throws SQLException {
        Optional<Customer> existing = findByPhone(conn, customer.getPhone());
        if (existing.isPresent()) {
            customer.setCustomerId(existing.get().getCustomerId());
            return customer.getCustomerId();
        }

        String sql = "INSERT INTO customer (customer_name, phone) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, customer.getCustomerName().trim());
            stmt.setString(2, customer.getPhone().trim());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    customer.setCustomerId(id);
                    return id;
                } else {
                    throw new SQLException("Creating customer failed, no generated ID obtained.");
                }
            }
        }
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getInt("customer_id"),
                rs.getString("customer_name"),
                rs.getString("phone")
        );
    }
}

