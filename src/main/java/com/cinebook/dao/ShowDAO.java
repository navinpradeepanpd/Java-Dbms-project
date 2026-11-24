package com.cinebook.dao;

import com.cinebook.model.Show;
import com.cinebook.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Show entity (mapped to movie_show table).
 */
public class ShowDAO {

    public List<Show> getAllShows() throws SQLException {
        List<Show> shows = new ArrayList<>();
        String sql = """
            SELECT s.show_id, s.movie_id, s.show_date, s.show_time, s.screen_name,
                   m.movie_name, m.language
            FROM movie_show s
            JOIN movie m ON s.movie_id = m.movie_id
            ORDER BY s.show_date, s.show_time
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                shows.add(mapResultSetToShow(rs));
            }
        }
        return shows;
    }

    public List<Show> getShowsByMovieId(int movieId) throws SQLException {
        List<Show> shows = new ArrayList<>();
        String sql = """
            SELECT s.show_id, s.movie_id, s.show_date, s.show_time, s.screen_name,
                   m.movie_name, m.language
            FROM movie_show s
            JOIN movie m ON s.movie_id = m.movie_id
            WHERE s.movie_id = ?
            ORDER BY s.show_date, s.show_time
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, movieId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    shows.add(mapResultSetToShow(rs));
                }
            }
        }
        return shows;
    }

    public Optional<Show> getShowById(int showId) throws SQLException {
        String sql = """
            SELECT s.show_id, s.movie_id, s.show_date, s.show_time, s.screen_name,
                   m.movie_name, m.language
            FROM movie_show s
            JOIN movie m ON s.movie_id = m.movie_id
            WHERE s.show_id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, showId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToShow(rs));
                }
            }
        }
        return Optional.empty();
    }

    private Show mapResultSetToShow(ResultSet rs) throws SQLException {
        Show show = new Show(
                rs.getInt("show_id"),
                rs.getInt("movie_id"),
                rs.getDate("show_date").toLocalDate(),
                rs.getTime("show_time").toLocalTime(),
                rs.getString("screen_name")
        );
        show.setMovieName(rs.getString("movie_name"));
        show.setLanguage(rs.getString("language"));
        return show;
    }
}

