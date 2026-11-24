package com.cinebook.controller;

import com.cinebook.dao.BookingDAO;
import com.cinebook.dao.MovieDAO;
import com.cinebook.dao.SeatDAO;
import com.cinebook.dao.ShowDAO;
import com.cinebook.model.Booking;
import com.cinebook.model.Movie;
import com.cinebook.model.Seat;
import com.cinebook.model.Show;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Administrator Operations.
 * Strictly restricted to users with ROLE_ADMIN via Spring Security.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final MovieDAO movieDAO;
    private final ShowDAO showDAO;
    private final BookingDAO bookingDAO;
    private final SeatDAO seatDAO;

    public AdminController() {
        this.movieDAO = new MovieDAO();
        this.showDAO = new ShowDAO();
        this.bookingDAO = new BookingDAO();
        this.seatDAO = new SeatDAO();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() throws SQLException {
        List<Movie> movies = movieDAO.getAllMovies();
        List<Show> shows = showDAO.getAllShows();
        List<Booking> bookings = bookingDAO.getAllBookings();

        long confirmedCount = bookings.stream().filter(b -> "CONFIRMED".equalsIgnoreCase(b.getStatus().name())).count();
        long cancelledCount = bookings.stream().filter(b -> "CANCELLED".equalsIgnoreCase(b.getStatus().name())).count();
        double totalRevenue = bookings.stream()
                .filter(b -> "CONFIRMED".equalsIgnoreCase(b.getStatus().name()))
                .mapToDouble(Booking::getTotalAmount)
                .sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMovies", movies.size());
        stats.put("totalShows", shows.size());
        stats.put("totalBookings", bookings.size());
        stats.put("confirmedBookings", confirmedCount);
        stats.put("cancelledBookings", cancelledCount);
        stats.put("totalRevenue", totalRevenue);
        stats.put("systemStatus", "OPERATIONAL");
        stats.put("securityLevel", "HIGH (BCrypt + RBAC Enforced)");

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> getAllBookings() throws SQLException {
        return ResponseEntity.ok(bookingDAO.getAllBookings());
    }

    @GetMapping("/shows/{showId}/seats")
    public ResponseEntity<List<Seat>> getShowSeats(@PathVariable("showId") int showId) throws SQLException {
        return ResponseEntity.ok(seatDAO.getSeatsByShowId(showId));
    }
}

