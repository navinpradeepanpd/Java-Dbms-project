package com.cinebook.dao;

import com.cinebook.enums.SeatStatus;
import com.cinebook.enums.SeatType;
import com.cinebook.model.Seat;
import com.cinebook.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Data Access Object for Seat entity.
 * Handles layout retrieval, row locking, and status transitions within transactions.
 */
public class SeatDAO {

    /**
     * Retrieves all seats for a given show, sorted in row (A-E) and column (1-8) order.
     */
    public List<Seat> getSeatsByShowId(int showId) throws SQLException {
        List<Seat> seats = new ArrayList<>();
        String sql = "SELECT seat_id, show_id, seat_code, seat_type, status FROM seat WHERE show_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, showId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seats.add(mapResultSetToSeat(rs));
                }
            }
        }

        // Sort reliably by row character, then numeric column: e.g. A1..A8, B1..B8, etc.
        seats.sort(Comparator.comparing((Seat s) -> s.getSeatCode().charAt(0))
                .thenComparingInt(s -> Integer.parseInt(s.getSeatCode().substring(1))));

        return seats;
    }

    /**
     * Locks and fetches seats by codes for an active transaction using SELECT ... FOR UPDATE.
     */
    public List<Seat> getSeatsByCodesForUpdate(Connection conn, int showId, List<String> seatCodes) throws SQLException {
        if (seatCodes == null || seatCodes.isEmpty()) {
            return Collections.emptyList();
        }

        // Build dynamic parameterized placeholders: IN (?, ?, ?)
        String placeholders = String.join(",", Collections.nCopies(seatCodes.size(), "?"));
        String sql = "SELECT seat_id, show_id, seat_code, seat_type, status FROM seat " +
                     "WHERE show_id = ? AND seat_code IN (" + placeholders + ") FOR UPDATE";

        List<Seat> seats = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, showId);
            for (int i = 0; i < seatCodes.size(); i++) {
                stmt.setString(i + 2, seatCodes.get(i).toUpperCase());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seats.add(mapResultSetToSeat(rs));
                }
            }
        }
        return seats;
    }

    /**
     * Batch updates seat statuses within an active transaction.
     */
    public void updateSeatStatuses(Connection conn, List<Integer> seatIds, SeatStatus status) throws SQLException {
        if (seatIds == null || seatIds.isEmpty()) return;

        String sql = "UPDATE seat SET status = ? WHERE seat_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int seatId : seatIds) {
                stmt.setString(1, status.name());
                stmt.setInt(2, seatId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    /**
     * Retrieves seat codes associated with a specific booking ID.
     */
    public List<String> getSeatCodesByBookingId(int bookingId) throws SQLException {
        List<String> seatCodes = new ArrayList<>();
        String sql = """
            SELECT s.seat_code
            FROM seat s
            JOIN booking_seat bs ON s.seat_id = bs.seat_id
            WHERE bs.booking_id = ?
            ORDER BY s.seat_code
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seatCodes.add(rs.getString("seat_code"));
                }
            }
        }
        return seatCodes;
    }

    /**
     * Non-destructively ensures full 168-seat capacity (12 rows A-L x 14 columns) for a show.
     * Uses INSERT IGNORE so existing seats and their booking records are 100% preserved.
     */
    public int ensure168SeatsForShow(Connection conn, int showId) throws SQLException {
        String sql = "INSERT IGNORE INTO seat (show_id, seat_code, seat_type, status) VALUES (?, ?, ?, 'AVAILABLE')";
        int inserted = 0;
        char[] rows = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L'};

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (char row : rows) {
                String type = SeatType.fromRow(row).name();
                for (int col = 1; col <= 14; col++) {
                    String seatCode = "" + row + col;
                    stmt.setInt(1, showId);
                    stmt.setString(2, seatCode);
                    stmt.setString(3, type);
                    stmt.addBatch();
                }
            }
            int[] results = stmt.executeBatch();
            for (int r : results) {
                if (r > 0) inserted += r;
            }
        }
        return inserted;
    }

    public int ensure168SeatsForShow(int showId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return ensure168SeatsForShow(conn, showId);
        }
    }

    private Seat mapResultSetToSeat(ResultSet rs) throws SQLException {
        return new Seat(
                rs.getInt("seat_id"),
                rs.getInt("show_id"),
                rs.getString("seat_code"),
                SeatType.valueOf(rs.getString("seat_type")),
                SeatStatus.fromString(rs.getString("status"))
        );
    }
}

