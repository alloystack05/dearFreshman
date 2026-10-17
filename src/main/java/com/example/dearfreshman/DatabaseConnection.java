package com.example.dearfreshman;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/dear_freshman";

    private static final String USER = "root";
    private static final String PASSWORD = "";

    private DatabaseConnection() {
        // Prevent creating instances of this utility class.
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static boolean testConnection() {
        try (Connection connection = getConnection()) {
            System.out.println(
                    "Successfully connected to dear_freshman!"
            );
            return true;
        } catch (SQLException e) {
            System.err.println(
                    "Failed to connect to dear_freshman."
            );
            System.err.println("Error: " + e.getMessage());
            return false;
        }
    }
}
