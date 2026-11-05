package com.scc.dao;

import com.scc.Model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    public boolean registerUser(String username, String password, String role, String status) {
        String salt = SecurityUtil.generateSalt();
        String hashedPassword = SecurityUtil.hashPassword(password, salt);
        String secureStoredValue = salt + ":" + hashedPassword;

        String sql = "INSERT INTO users (username, password, role, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, secureStoredValue);
            pstmt.setString(3, role.toUpperCase());
            pstmt.setString(4, status.toUpperCase());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Registration failed (Username might already exist): " + e.getMessage());
            return false;
        }
    }

    public String loginUser(String username, String password) {
        String sql = "SELECT user_id, password, role FROM users WHERE username = ? AND status = 'ACTIVE'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String storedCredentials = rs.getString("password");
                    String role = rs.getString("role");

                    if (storedCredentials.contains(":")) {
                        String[] parts = storedCredentials.split(":");
                        if (SecurityUtil.verifyPassword(password, parts[0], parts[1])) {
                            // MANDATORY LOG #1: Authentication (Login)
                            ActivityLogDao.logActivity(userId, "AUTHENTICATION", "User " + username + " (ID: " + userId + ") logged in.");
                            return role;
                        }
                    } else if (storedCredentials.equals(password)) {
                        ActivityLogDao.logActivity(userId, "AUTHENTICATION", "User " + username + " (ID: " + userId + ") logged in.");
                        return role;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Login Error: " + e.getMessage());
        }
        return null;
    }

    public void logLogout(int userId, String username) {
        if (userId > 0 && username != null) {
            // MANDATORY LOG #1: Authentication (Logout)
            ActivityLogDao.logActivity(userId, "AUTHENTICATION", "User " + username + " (ID: " + userId + ") logged out.");
        }
    }

    public int getUserIdByUsername(String username) {
        String sql = "SELECT user_id FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            // ignore
        }
        return -1;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, role, status FROM users";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        "[SECURED]",
                        rs.getString("role"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching users: " + e.getMessage());
        }
        return users;
    }

    public List<User> getPendingUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, role, status FROM users WHERE status = 'PENDING'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        "[SECURED]",
                        rs.getString("role"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching pending users: " + e.getMessage());
        }
        return users;
    }

    public boolean approveUser(int adminId, int targetUserId) {
        String sql = "UPDATE users SET status = 'ACTIVE' WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, targetUserId);
            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                // MANDATORY LOG #2: Account Management (User Approved)
                ActivityLogDao.logActivity(adminId, "ACCOUNT_MANAGEMENT", "Admin " + adminId + " approved User " + targetUserId);
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error approving user: " + e.getMessage());
            return false;
        }
    }

    public boolean approveUser(int userId) {
        return approveUser(userId, userId);
    }

    public boolean updateUserRoleAndStatus(int userId, String newRole, String newStatus) {
        String sql = "UPDATE users SET role = ?, status = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newRole.toUpperCase());
            pstmt.setString(2, newStatus.toUpperCase());
            pstmt.setInt(3, userId);
            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                // MANDATORY LOG #2: Account Management (Role Edit)
                ActivityLogDao.logActivity(userId, "ACCOUNT_MANAGEMENT", "Role changed to " + newRole.toUpperCase());
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }
}