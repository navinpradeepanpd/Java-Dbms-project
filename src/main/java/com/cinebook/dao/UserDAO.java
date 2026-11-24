package com.cinebook.dao;

import com.cinebook.model.User;
import com.cinebook.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Optional;

/**
 * Data Access Object for User entity with native JDBC.
 */
public class UserDAO {

    public Optional<User> findByUsername(String username) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return findByUsername(conn, username);
        }
    }

    public Optional<User> findByUsername(Connection conn, String username) throws SQLException {
        String sql = "SELECT user_id, username, password, full_name, role, customer_id, created_at FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<User> findById(int userId) throws SQLException {
        String sql = "SELECT user_id, username, password, full_name, role, customer_id, created_at FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    public int createUser(Connection conn, User user) throws SQLException {
        String sql = "INSERT INTO users (username, password, full_name, role, customer_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername().trim());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getFullName().trim());
            stmt.setString(4, user.getRole() != null ? user.getRole().trim().toUpperCase() : "CUSTOMER");
            if (user.getCustomerId() != null) {
                stmt.setInt(5, user.getCustomerId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    user.setUserId(id);
                    return id;
                } else {
                    throw new SQLException("Creating user failed, no generated ID obtained.");
                }
            }
        }
    }

    public int createUser(User user) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return createUser(conn, user);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setFullName(rs.getString("full_name"));
        u.setRole(rs.getString("role"));
        int cId = rs.getInt("customer_id");
        if (!rs.wasNull()) {
            u.setCustomerId(cId);
        }
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }
}
