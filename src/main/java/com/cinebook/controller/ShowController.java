package com.cinebook.controller;

import com.cinebook.model.Seat;
import com.cinebook.model.Show;
import com.cinebook.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Web REST Controller for Movie Shows & Seat inventory.
 * Delegates directly to the existing BookingService, ShowDAO & SeatDAO.
 */
@RestController
@RequestMapping("/api/shows")
@CrossOrigin(origins = "*")
public class ShowController {

    private final BookingService bookingService;

    public ShowController() {
        this.bookingService = new BookingService();
    }

    @GetMapping
    public List<Show> getAllShows() throws SQLException {
        return bookingService.getAllShows();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Show> getShowById(@PathVariable int id) throws SQLException {
        Optional<Show> opt = bookingService.getAllShows().stream()
                .filter(s -> s.getShowId() == id)
                .findFirst();
        return opt.map(ResponseEntity::ok)
                  .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/seats")
    public List<Seat> getSeatMap(@PathVariable int id) throws SQLException {
        return bookingService.getSeatMapForShow(id);
    }
}

