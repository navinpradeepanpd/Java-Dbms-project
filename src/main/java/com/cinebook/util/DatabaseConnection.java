package com.cinebook.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * JDBC Connection Factory and utility for CineBook.
 * Centralizes database configuration loading, connection creation via DriverManager,
 * and automatic database schema initialization.
 */
public class DatabaseConnection {

    private static String url;
    private static String user;
    private static String password;
    private static String driver;

    static {
        loadProperties();
    }

    private DatabaseConnection() {
        // Private constructor to prevent instantiation (Utility class)
    }

    private static void loadProperties() {
        Properties props = new Properties();
        try (InputStream is = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                props.load(is);
                driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
                url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/cinebook_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
                user = props.getProperty("db.user", "root");
                password = props.getProperty("db.password", "root");
            } else {
                // Fallback default configuration if db.properties is not in classpath
                driver = "com.mysql.cj.jdbc.Driver";
                url = "jdbc:mysql://localhost:3306/cinebook_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
                user = "root";
                password = "root";
            }
            Class.forName(driver);
        } catch (Exception e) {
            System.err.println("Warning: Could not load db.properties, using defaults. Reason: " + e.getMessage());
            driver = "com.mysql.cj.jdbc.Driver";
            url = "jdbc:mysql://localhost:3306/cinebook_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            user = "root";
            password = "root";
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException ex) {
                System.err.println("Error: MySQL JDBC Driver class not found: " + ex.getMessage());
            }
        }
    }

    /**
     * Obtains a new database connection.
     * Callers must close or manage this connection in try-with-resources.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Closes an AutoCloseable (Connection, Statement, ResultSet) safely without throwing exceptions.
     */
    public static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Executes a SQL script file from the classpath or filesystem.
     * Useful for automated schema creation and seed data loading.
     */
    public static void executeSqlScript(String resourcePath) {
        try (InputStream is = DatabaseConnection.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("SQL script not found in classpath: " + resourcePath);
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                 Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {

                StringBuilder sqlBatch = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    // Strip inline SQL comments
                    int commentIndex = line.indexOf("--");
                    if (commentIndex != -1) {
                        line = line.substring(0, commentIndex);
                    }
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#") || line.startsWith("/*")) {
                        continue;
                    }
                    sqlBatch.append(line).append(" ");
                    if (line.endsWith(";")) {
                        String query = sqlBatch.toString().trim();
                        // Remove trailing semicolon
                        query = query.substring(0, query.length() - 1).trim();
                        if (!query.isEmpty()) {
                            try {
                                stmt.execute(query);
                            } catch (SQLException e) {
                                // Ignore harmless duplicate / already exists notices
                                String msg = e.getMessage().toLowerCase();
                                if (!msg.contains("already exists") && !msg.contains("duplicate")) {
                                    System.err.println("Notice executing SQL line: " + e.getMessage());
                                }
                            }
                        }
                        sqlBatch.setLength(0);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error executing script " + resourcePath + ": " + e.getMessage());
        }
    }

    /**
     * Self-test method to verify database connectivity and schema readiness.
     */
    public static void main(String[] args) {
        System.out.println("=== CineBook Database Connection Test ===");
        try (Connection conn = getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("[SUCCESS] Connected to database: " + conn.getCatalog());
            System.out.println("[INFO] RDBMS Product: " + meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());
            System.out.println("[INFO] Driver: " + meta.getDriverName() + " " + meta.getDriverVersion());

            System.out.println("\n[INFO] Initializing schema...");
            executeSqlScript("schema.sql");
            System.out.println("[SUCCESS] Schema executed.");

            System.out.println("\n[INFO] Initializing seed data...");
            executeSqlScript("data.sql");
            System.out.println("[SUCCESS] Seed data executed.");

        } catch (SQLException e) {
            System.err.println("[FAILURE] Could not connect to MySQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
