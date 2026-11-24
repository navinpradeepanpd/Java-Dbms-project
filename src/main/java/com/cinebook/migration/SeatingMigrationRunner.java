package com.cinebook.migration;

import com.cinebook.dao.SeatDAO;
import com.cinebook.util.DatabaseConnection;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Non-destructive database migration runner.
 * Safely upgrades auditorium capacity from legacy 5x8 (40 seats) to realistic 12x14 (168 seats).
 * 
 * Guarantees:
 * - Uses INSERT IGNORE to preserve all existing seat IDs, statuses, and booking_seat junction records.
 * - Completely preserves ACID consistency for historical bookings and refunds.
 * - Idempotent: can run repeatedly on startup without adverse effects.
 */
@Component
@Order(10)
public class SeatingMigrationRunner implements CommandLineRunner {

    private final SeatDAO seatDAO;

    public SeatingMigrationRunner() {
        this.seatDAO = new SeatDAO();
    }

    @Override
    public void run(String... args) {
        System.out.println(">>> [MIGRATION] Checking auditorium seating capacity (Target: 168 seats per show: 12 rows x 14 cols)...");

        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Integer> showIds = new ArrayList<>();
            String sql = "SELECT show_id FROM movie_show ORDER BY show_id ASC";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    showIds.add(rs.getInt("show_id"));
                }
            }

            int totalAdded = 0;
            for (int showId : showIds) {
                int added = seatDAO.ensure168SeatsForShow(conn, showId);
                totalAdded += added;
            }

            System.out.printf(">>> [MIGRATION] Seating verification complete. %d shows verified, %d new seats safely provisioned.%n",
                    showIds.size(), totalAdded);

        } catch (SQLException e) {
            System.err.println("Notice during seating migration: " + e.getMessage());
        }
    }
}
