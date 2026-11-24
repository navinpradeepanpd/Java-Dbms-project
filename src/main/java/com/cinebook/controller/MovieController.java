package com.cinebook.controller;

import com.cinebook.model.Movie;
import com.cinebook.service.BookingService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;

/**
 * Web REST Controller for Movie resource.
 * Delegates directly to the existing BookingService & MovieDAO.
 */
@RestController
@RequestMapping("/api/movies")
@CrossOrigin(origins = "*")
public class MovieController {

    private final BookingService bookingService;

    public MovieController() {
        this.bookingService = new BookingService();
    }

    @GetMapping
    public List<Movie> getAllMovies() throws SQLException {
        return bookingService.getAllMovies();
    }
}

