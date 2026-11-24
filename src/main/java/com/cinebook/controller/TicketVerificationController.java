package com.cinebook.controller;

import com.cinebook.dao.BookingDAO;
import com.cinebook.model.Booking;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Public Server-Side Digital Ticket Verification Endpoint.
 * Allows QR code verification at the cinema entrance box office without exposing
 * private customer details (e.g. phone number, billing address).
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketVerificationController {

    private final BookingDAO bookingDAO;

    public TicketVerificationController() {
        this.bookingDAO = new BookingDAO();
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyTicket(
            @RequestParam("bookingId") int bookingId,
            @RequestParam(value = "token", required = false) String token) throws SQLException {

        Optional<Booking> opt = bookingDAO.getBookingById(bookingId);
        Map<String, Object> resp = new HashMap<>();

        if (opt.isEmpty()) {
            resp.put("valid", false);
            resp.put("message", "Invalid Ticket: No reservation found with reference #" + bookingId);
            resp.put("status", "NOT_FOUND");
            return ResponseEntity.ok(resp);
        }

        Booking b = opt.get();
        boolean isConfirmed = "CONFIRMED".equalsIgnoreCase(b.getStatus().name());

        resp.put("valid", isConfirmed);
        resp.put("bookingId", b.getBookingId());
        resp.put("bookingReference", "CB-2026-" + b.getBookingId());
        resp.put("movieTitle", b.getMovieTitle());
        resp.put("screenName", b.getScreenName() != null ? b.getScreenName() : "Screen 1");
        resp.put("showDate", b.getShowDate() != null ? b.getShowDate().toString() : "");
        resp.put("showTime", b.getShowTime() != null ? b.getShowTime().toString() : "");
        resp.put("seats", b.getBookedSeats());
        resp.put("seatCount", b.getBookedSeats() != null ? b.getBookedSeats().size() : 0);
        resp.put("status", b.getStatus().name());
        resp.put("verifiedAt", LocalDateTime.now().toString());

        if (!isConfirmed) {
            resp.put("message", "Ticket Void: This reservation was cancelled and refunded.");
        } else {
            resp.put("message", "Valid Cinema Boarding Pass. Admittance granted.");
        }

        return ResponseEntity.ok(resp);
    }
}
