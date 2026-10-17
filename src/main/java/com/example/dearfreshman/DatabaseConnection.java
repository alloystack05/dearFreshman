package com.example.dearfreshman;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.stream.Collectors;

public class DatabaseConnection {

    private static final String DB_TYPE = System.getenv().getOrDefault("DEAR_FRESHMAN_DB_TYPE", "H2").toUpperCase();

    // MySQL env-driven defaults
    private static final String MYSQL_URL = System.getenv().getOrDefault(
            "DEAR_FRESHMAN_DB_URL", "jdbc:mysql://localhost:3306/dear_freshman");
    private static final String MYSQL_USER = System.getenv().getOrDefault(
            "DEAR_FRESHMAN_DB_USER", "root");
    private static final String MYSQL_PASSWORD = System.getenv().getOrDefault(
            "DEAR_FRESHMAN_DB_PASSWORD", "");

    // H2 defaults (file-based DB in project folder; MODE=MySQL improves compatibility)
    private static final String H2_URL = System.getenv().getOrDefault(
            "DEAR_FRESHMAN_H2_URL", "jdbc:h2:./data/dear_freshman;MODE=MySQL;AUTO_SERVER=TRUE");
    private static final String H2_USER = System.getenv().getOrDefault("DEAR_FRESHMAN_H2_USER", "sa");
    private static final String H2_PASSWORD = System.getenv().getOrDefault("DEAR_FRESHMAN_H2_PASSWORD", "");

    private DatabaseConnection() {
        // Prevent creating instances of this utility class.
    }

    public static Connection getConnection() throws SQLException {
        if ("MYSQL".equals(DB_TYPE)) {
            return DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD);
        } else {
            Connection conn = DriverManager.getConnection(H2_URL, H2_USER, H2_PASSWORD);
            ensureSchema(conn);
            return conn;
        }
    }

    private static volatile boolean schemaEnsured = false;

    private static synchronized void ensureSchema(Connection conn) {
        if (schemaEnsured) return;
        try {
            DatabaseMetaData md = conn.getMetaData();
            try (ResultSet rs = md.getTables(null, null, "PLAYERS", null)) {
                if (rs.next()) {
                    schemaEnsured = true;
                    return;
                }
            }

            try (InputStream in = DatabaseConnection.class.getResourceAsStream("/db/schema.sql")) {
                if (in == null) return;
                String sql = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                        .lines().collect(Collectors.joining("\n"));
                for (String stmt : sql.split(";\\s*\n")) {
                    String trimmed = stmt.trim();
                    if (trimmed.isEmpty()) continue;
                    try (Statement s = conn.createStatement()) {
                        s.execute(trimmed);
                    }
                }
            }
            schemaEnsured = true;
        } catch (Exception e) {
            System.err.println("Failed to ensure DB schema: " + e.getMessage());
        }
    }

    public static boolean testConnection() {
        try (Connection connection = getConnection()) {
            System.out.println("Successfully connected to " + DB_TYPE + " database.");
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to connect to database (" + DB_TYPE + "): " + e.getMessage());
            return false;
        }
    }
}
