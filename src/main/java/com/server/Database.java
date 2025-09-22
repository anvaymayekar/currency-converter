package com.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String URL = "jdbc:sqlite:auth.db"; // your DB file

    public static void initialize() {
        try (Connection conn = DriverManager.getConnection(URL)) {
            if (conn != null) {
                String sql = "CREATE TABLE IF NOT EXISTS users ("
                        + " id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + " username TEXT NOT NULL UNIQUE, "
                        + " password_hash TEXT NOT NULL, "
                        + " salt TEXT NOT NULL"
                        + ");";

                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                    System.out.println("Database ready.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database initialization failed: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
