package com.scc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    public static final String DB_URL = "jdbc:sqlite:SSQMS.db";

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(DB_URL);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON;");
            // Enable Write-Ahead Logging to allow concurrent reads and writes
            statement.execute("PRAGMA journal_mode = WAL;");
            // Wait up to 5 seconds if the database is temporarily locked instead of throwing an error
            statement.execute("PRAGMA busy_timeout = 5000;");
        }
        return connection;
    }
}