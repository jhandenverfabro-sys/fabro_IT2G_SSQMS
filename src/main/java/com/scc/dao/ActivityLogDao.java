package com.scc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ActivityLogDao {
    public static void logActivity(int userId, String category, String message) {
        String sql = "INSERT INTO activity_logs (user_id, category, message, timestamp) VALUES (?, ?, ?, datetime('now'))";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, category);
            pstmt.setString(3, message);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging activity: " + e.getMessage());
        }
    }
}