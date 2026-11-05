package com.scc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ServiceDao {

    public boolean addService(String name, String description, int counterNumber, String status) {
        String sql = "INSERT INTO Services (service_name, description, counter_number, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setInt(3, counterNumber);
            pstmt.setString(4, status);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding service: " + e.getMessage());
            return false;
        }
    }

    public boolean updateService(int serviceId, String name, String description, int counterNumber, String status) {
        String sql = "UPDATE Services SET service_name = ?, description = ?, counter_number = ?, status = ? WHERE service_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setInt(3, counterNumber);
            pstmt.setString(4, status);
            pstmt.setInt(5, serviceId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating service: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteService(int serviceId) {
        String sql = "DELETE FROM Services WHERE service_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, serviceId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting service: " + e.getMessage());
            return false;
        }
    }
}