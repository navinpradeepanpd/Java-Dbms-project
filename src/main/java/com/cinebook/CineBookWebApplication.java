package com.cinebook;

import com.cinebook.util.DatabaseConnection;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot Web Application entry point for CineBook.
 * Boots embedded Tomcat server and exposes Web UI and REST Controllers
 * while delegating all data persistence and business logic to the existing
 * JDBC DAOs and BookingService.
 */
@SpringBootApplication
public class CineBookWebApplication {

    public static void main(String[] args) {
        // Auto-initialize MySQL schema and seed data on startup
        try {
            DatabaseConnection.executeSqlScript("schema.sql");
            DatabaseConnection.executeSqlScript("data.sql");
        } catch (Exception e) {
            System.err.println("Notice during database startup check: " + e.getMessage());
        }

        SpringApplication.run(CineBookWebApplication.class, args);
    }
}

