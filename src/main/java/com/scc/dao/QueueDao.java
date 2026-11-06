package com.scc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class QueueDao {

    // Domain Business Action: TICKET_CREATED
    public boolean createTicket(int studentId, String username, String serviceType) {
        String sql = "INSERT INTO queue (student_id, service_id, status) VALUES (?, 1, 'WAITING')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, studentId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int queueId = rs.getInt(1);
                        ActivityLogDao.logAction(
                                studentId,
                                "TICKET_CREATED",
                                "Processed queue ticket #" + queueId + " for Student " + username + " - Service: " + serviceType
                        );
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error generating queue ticket: " + e.getMessage());
        }
        return false;
    }

    // Domain Lifecycle Action: STATUS_UPDATED
    public boolean updateTicketStatus(int staffUserId, int queueId, String oldStatus, String newStatus) {
        String sql = "UPDATE queue SET status = ? WHERE queue_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus.toUpperCase());
            pstmt.setInt(2, queueId);

            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                ActivityLogDao.logAction(
                        staffUserId,
                        "STATUS_UPDATED",
                        "Updated Ticket #" + queueId + " status from " + oldStatus.toUpperCase() + " to " + newStatus.toUpperCase()
                );
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error updating queue status: " + e.getMessage());
        }
        return false;
    }
}