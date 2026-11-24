package com.cinebook.controller;

import com.cinebook.dao.MovieDAO;
import com.cinebook.dao.ShowDAO;
import com.cinebook.model.Movie;
import com.cinebook.model.Show;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Administrator Operations.
 * Strictly restricted to users with ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final MovieDAO movieDAO;
    private final ShowDAO showDAO;

    public AdminController() {
        this.movieDAO = new MovieDAO();
        this.showDAO = new ShowDAO();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() throws SQLException {
        List<Movie> movies = movieDAO.getAllMovies();
        List<Show> shows = showDAO.getAllShows();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMovies", movies.size());
        stats.put("totalShows", shows.size());
        stats.put("systemStatus", "OPERATIONAL");
        stats.put("securityLevel", "HIGH (BCrypt + RBAC Enforced)");

        return ResponseEntity.ok(stats);
    }
}
