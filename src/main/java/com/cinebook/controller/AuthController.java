package com.cinebook.controller;

import com.cinebook.dao.CustomerDAO;
import com.cinebook.dao.UserDAO;
import com.cinebook.dto.RegistrationRequest;
import com.cinebook.model.Customer;
import com.cinebook.model.User;
import com.cinebook.security.CustomUserDetails;
import com.cinebook.util.DatabaseConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for User Registration and Session Information.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserDAO userDAO;
    private final CustomerDAO customerDAO;
    private final PasswordEncoder passwordEncoder;

    public AuthController(PasswordEncoder passwordEncoder) {
        this.userDAO = new UserDAO();
        this.customerDAO = new CustomerDAO();
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegistrationRequest req) {
        Map<String, Object> resp = new HashMap<>();

        if (req.getUsername() == null || req.getUsername().trim().isEmpty()) {
            resp.put("success", false);
            resp.put("message", "Username/Phone is required.");
            return ResponseEntity.badRequest().body(resp);
        }

        if (req.getPassword() == null || req.getPassword().length() < 6) {
            resp.put("success", false);
            resp.put("message", "Password must be at least 6 characters.");
            return ResponseEntity.badRequest().body(resp);
        }

        if (req.getFullName() == null || req.getFullName().trim().isEmpty()) {
            resp.put("success", false);
            resp.put("message", "Full name is required.");
            return ResponseEntity.badRequest().body(resp);
        }

        String username = req.getUsername().trim();
        String phone = req.getPhone() != null && !req.getPhone().trim().isEmpty() ? req.getPhone().trim() : username;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Check if username already exists
                Optional<User> existingUser = userDAO.findByUsername(conn, username);
                if (existingUser.isPresent()) {
                    conn.rollback();
                    resp.put("success", false);
                    resp.put("message", "Username or phone already registered. Please login.");
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(resp);
                }

                // Check or create customer record in customer table
                Customer customer = new Customer(req.getFullName().trim(), phone);
                int customerId = customerDAO.createOrGetCustomer(conn, customer);

                // Create user account
                String encodedPassword = passwordEncoder.encode(req.getPassword());
                User user = new User(username, encodedPassword, req.getFullName().trim(), "CUSTOMER", customerId);
                userDAO.createUser(conn, user);

                conn.commit();

                resp.put("success", true);
                resp.put("message", "Account registered successfully! Please login with your credentials.");
                resp.put("redirectUrl", "/login.html");
                return ResponseEntity.status(HttpStatus.CREATED).body(resp);

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            resp.put("success", false);
            resp.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        Map<String, Object> resp = new HashMap<>();
        if (authentication == null || !authentication.isAuthenticated()) {
            resp.put("authenticated", false);
            return ResponseEntity.ok(resp);
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            resp.put("authenticated", true);
            resp.put("username", userDetails.getUsername());
            resp.put("fullName", userDetails.getFullName());
            resp.put("role", userDetails.getRole());
            resp.put("customerId", userDetails.getCustomerId());
            return ResponseEntity.ok(resp);
        }

        resp.put("authenticated", true);
        resp.put("username", authentication.getName());
        return ResponseEntity.ok(resp);
    }
}
