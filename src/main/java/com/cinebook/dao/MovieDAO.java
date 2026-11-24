package com.cinebook.dao;

import com.cinebook.model.Movie;
import com.cinebook.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Movie entity.
 * Uses PreparedStatement for secure query execution.
 */
public class MovieDAO {

    public List<Movie> getAllMovies() throws SQLException {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT movie_id, movie_name, duration, language, genre FROM movie ORDER BY movie_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                movies.add(mapResultSetToMovie(rs));
            }
        }
        return movies;
    }

    public Optional<Movie> getMovieById(int movieId) throws SQLException {
        String sql = "SELECT movie_id, movie_name, duration, language, genre FROM movie WHERE movie_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, movieId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMovie(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Movie> findByName(String name) throws SQLException {
        if (name == null || name.trim().isEmpty()) return Optional.empty();
        String sql = "SELECT movie_id, movie_name, duration, language, genre FROM movie WHERE LOWER(TRIM(movie_name)) = LOWER(TRIM(?))";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMovie(rs));
                }
            }
        }
        return Optional.empty();
    }

    public int createMovie(Connection conn, String movieName, int duration, String language, String genre) throws SQLException {
        String sql = "INSERT INTO movie (movie_name, duration, language, genre) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, movieName.trim());
            stmt.setInt(2, duration > 0 ? duration : 120);
            stmt.setString(3, language != null ? language.trim() : "English");
            stmt.setString(4, genre != null ? genre.trim() : "Cinema");
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to create movie record, no generated ID.");
    }

    private Movie mapResultSetToMovie(ResultSet rs) throws SQLException {
        return new Movie(
                rs.getInt("movie_id"),
                rs.getString("movie_name"),
                rs.getInt("duration"),
                rs.getString("language"),
                rs.getString("genre")
        );
    }
}

