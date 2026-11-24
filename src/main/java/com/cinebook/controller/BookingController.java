package com.cinebook.controller;

import com.cinebook.dto.BookingRequest;
import com.cinebook.dto.BookingResponse;
import com.cinebook.dto.CancellationResponse;
import com.cinebook.exception.BookingNotFoundException;
import com.cinebook.exception.CineBookException;
import com.cinebook.model.Booking;
import com.cinebook.model.Customer;
import com.cinebook.service.BookingService;
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

    public BookingController() {
        this.bookingService = new BookingService();
    }

    @PostMapping
    public ResponseEntity<BookingResponse> bookTickets(@RequestBody BookingRequest req) throws CineBookException {
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

        return ResponseEntity.ok(new BookingResponse(true, "Booking confirmed successfully!", booking));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable("id") int id) throws SQLException, BookingNotFoundException {
        Optional<Booking> opt = bookingService.getBookingDetails(id);
        if (opt.isEmpty()) {
            throw new BookingNotFoundException("Booking #" + id + " was not found.");
        }
        return ResponseEntity.ok(opt.get());
    }

    @GetMapping("/customer/{phone}")
    public List<Booking> getCustomerBookings(@PathVariable("phone") String phone) throws SQLException {
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

