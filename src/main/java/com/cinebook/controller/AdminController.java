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

    @org.springframework.web.bind.annotation.PostMapping("/screenings")
    public ResponseEntity<Map<String, Object>> createScreening(@org.springframework.web.bind.annotation.RequestBody Map<String, Object> req) throws SQLException {
        String movieTitle = (String) req.get("title");
        if (movieTitle == null || movieTitle.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Movie title is required."));
        }

        String showDateStr = (String) req.get("showDate");
        String showTimeStr = (String) req.get("showTime");
        String screenName = (String) req.getOrDefault("screenName", "Screen 1");
        if (screenName != null && screenName.length() > 20) {
            screenName = screenName.substring(0, 20);
        }
        String language = (String) req.getOrDefault("language", "English");
        String genre = (String) req.getOrDefault("genre", "Cinema");
        int duration = req.containsKey("duration") ? Integer.parseInt(req.get("duration").toString()) : 140;

        if (showDateStr == null || showTimeStr == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Show date and show time are required."));
        }

        java.time.LocalDate showDate = java.time.LocalDate.parse(showDateStr.trim());
        java.time.LocalTime showTime = java.time.LocalTime.parse(showTimeStr.trim());

        try (java.sql.Connection conn = com.cinebook.util.DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Check if movie already exists in database
                java.util.Optional<Movie> optMovie = movieDAO.findByName(movieTitle);
                int movieId;
                if (optMovie.isPresent()) {
                    movieId = optMovie.get().getMovieId();
                } else {
                    movieId = movieDAO.createMovie(conn, movieTitle, duration, language, genre);
                }

                // Create show record
                int showId = showDAO.createShow(conn, movieId, showDate, showTime, screenName);

                // Provision all 168 seats for this show (12 rows x 14 columns)
                int seatsCreated = seatDAO.ensure168SeatsForShow(conn, showId);

                conn.commit();

                Map<String, Object> resp = new HashMap<>();
                resp.put("success", true);
                resp.put("message", "Screening scheduled successfully! 168 seats provisioned.");
                resp.put("showId", showId);
                resp.put("movieId", movieId);
                resp.put("seatsProvisioned", seatsCreated);
                return ResponseEntity.ok(resp);

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}

