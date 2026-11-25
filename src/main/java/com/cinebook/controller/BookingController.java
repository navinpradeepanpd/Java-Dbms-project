package com.cinebook.controller;

import com.cinebook.dao.BookingDAO;
import com.cinebook.dao.UserDAO;
import com.cinebook.dto.BookingRequest;
import com.cinebook.dto.BookingResponse;
import com.cinebook.dto.CancellationResponse;
import com.cinebook.exception.BookingNotFoundException;
import com.cinebook.exception.CineBookException;
import com.cinebook.model.Booking;
import com.cinebook.model.Customer;
import com.cinebook.model.User;
import com.cinebook.service.BookingService;
import com.cinebook.security.CustomUserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Web REST Controller for Booking and Cancellation transactions.
 * Calls existing Bookable / BookingService interface for strict ACID guarantees.
 */
@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;
    private final BookingDAO bookingDAO;
    private final UserDAO userDAO;

    public BookingController() {
        this.bookingService = new BookingService();
        this.bookingDAO = new BookingDAO();
        this.userDAO = new UserDAO();
    }

    @PostMapping
    public ResponseEntity<BookingResponse> bookTickets(@RequestBody BookingRequest req, Authentication authentication) throws CineBookException {
        if (req.getCustomerName() == null || req.getCustomerName().trim().isEmpty()) {
            throw new CineBookException("Customer name cannot be empty.");
        }
        if (req.getPhone() == null || !req.getPhone().trim().matches("^[0-9]{7,15}$")) {
            throw new CineBookException("Please enter a valid phone number (7-15 digits).");
        }
        if (req.getSeatCodes() == null || req.getSeatCodes().isEmpty()) {
            throw new CineBookException("Please select at least one seat to book.");
        }

        Customer customer = new Customer(req.getCustomerName().trim(), req.getPhone().trim());
        Booking booking = bookingService.bookSeats(customer, req.getShowId(), req.getSeatCodes(), req.getCouponCode());

        // Associate customer with logged in user account if logged in
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            try {
                userDAO.updateUserCustomerId(userDetails.getUserId(), booking.getCustomerId());
            } catch (SQLException ignored) {}
        }

        return ResponseEntity.ok(new BookingResponse(true, "Booking confirmed successfully!", booking));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable("id") int id, Authentication authentication) throws SQLException, BookingNotFoundException {
        Optional<Booking> opt = bookingService.getBookingDetails(id);
        if (opt.isEmpty()) {
            throw new BookingNotFoundException("Booking #" + id + " was not found.");
        }
        Booking b = opt.get();

        // Enforce ownership check: Customer can only view their own booking unless they are an ADMIN
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            boolean isAdmin = "ADMIN".equalsIgnoreCase(userDetails.getRole());
            if (!isAdmin) {
                Integer userCustId = userDetails.getCustomerId();
                String username = userDetails.getUsername();

                // Check fresh user record from database in case user was just linked on booking
                if (userCustId == null || userCustId != b.getCustomerId()) {
                    try {
                        Optional<User> fresh = userDAO.findById(userDetails.getUserId());
                        if (fresh.isPresent() && fresh.get().getCustomerId() != null) {
                            userCustId = fresh.get().getCustomerId();
                        }
                    } catch (SQLException ignored) {}
                }

                boolean matches = false;
                if (userCustId != null && userCustId.equals(b.getCustomerId())) {
                    matches = true;
                } else if (b.getCustomerPhone() != null && b.getCustomerPhone().equals(username)) {
                    matches = true;
                }

                if (!matches) {
                    throw new AccessDeniedException("Access denied: You do not have permission to view reservation #" + id);
                }
            }
        }

        return ResponseEntity.ok(b);
    }

    @GetMapping("/my")
    public List<Booking> getMyBookings(Authentication authentication) throws SQLException {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            boolean isAdmin = "ADMIN".equalsIgnoreCase(userDetails.getRole());
            if (isAdmin) {
                // Administrators viewing reservations see all customer bookings across the system
                return bookingDAO.getAllBookings();
            }

            // Customer account: look up by customerId first if present, otherwise by phone/username
            Integer custId = userDetails.getCustomerId();
            if (custId == null) {
                try {
                    Optional<User> fresh = userDAO.findById(userDetails.getUserId());
                    if (fresh.isPresent() && fresh.get().getCustomerId() != null) {
                        custId = fresh.get().getCustomerId();
                    }
                } catch (Exception ignored) {}
            }

            if (custId != null) {
                List<Booking> byId = bookingDAO.getBookingsByCustomerId(custId, false);
                if (!byId.isEmpty()) {
                    return byId;
                }
            }

            String phone = userDetails.getUsername();
            // Return all customer bookings (confirmed + cancelled) for history view
            return bookingService.getCustomerBookings(phone, false);
        }
        return List.of();
    }

    @GetMapping("/customer/{phone}")
    public List<Booking> getCustomerBookings(@PathVariable("phone") String phone, Authentication authentication) throws SQLException {
        // Enforce ownership check: Customer can only view their own phone bookings unless they are an ADMIN
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            boolean isAdmin = "ADMIN".equalsIgnoreCase(userDetails.getRole());
            if (!isAdmin && !phone.equals(userDetails.getUsername())) {
                throw new AccessDeniedException("Access denied: You can only query your own bookings.");
            }
        }

        return bookingService.getCustomerBookings(phone);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<CancellationResponse> cancelBooking(@PathVariable("id") int id) throws CineBookException, SQLException {
        Optional<Booking> opt = bookingService.getBookingDetails(id);
        if (opt.isEmpty()) {
            throw new BookingNotFoundException("Booking #" + id + " was not found.");
        }
        Booking b = opt.get();
        double refundAmount = b.getTotalAmount() * BookingService.CANCELLATION_REFUND_PERCENT;

        bookingService.cancelBooking(id);

        return ResponseEntity.ok(new CancellationResponse(
                true,
                "Booking successfully cancelled and 80% refund issued.",
                id,
                "CANCELLED",
                refundAmount
        ));
    }
}

