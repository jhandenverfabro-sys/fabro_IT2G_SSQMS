package com.scc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

public class userDao {
    public boolean registerUser(String username, String password, String role, String status){

        String sql = "INSERT INTO user (username, password, role, status) VALUES (?, ?, ?, 'INACTIVE')";

        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role.toUpperCase());
            pstmt.executeUpdate();
            System.out.println("-> Success Registration, waiting for Approval!");
            return true;

        } catch (SQLException e) {
            System.err.println("Failed"+ e.getMessage());
            return false;
        }

    }

    public String loginUser(String username, String password) {

        String sql = "SELECT role FROM User WHERE username = ? AND password = ? AND status = 'ACTIVE'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("role");
            }

        } catch (Exception e) {
            System.out.println("Login Error: " + e.getMessage());
        }

        return null;
    }
}
