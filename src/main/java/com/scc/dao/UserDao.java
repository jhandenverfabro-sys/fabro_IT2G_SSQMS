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
            System.err.println("Registration failed: " + e.getMessage());
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
                    String role = rs.getString("role").toUpperCase();

                    boolean authenticated = false;
                    if (storedCredentials.contains(":")) {
                        String[] parts = storedCredentials.split(":");
                        if (SecurityUtil.verifyPassword(password, parts[0], parts[1])) {
                            authenticated = true;
                        }
                    } else if (storedCredentials.equals(password)) {
                        authenticated = true;
                    }

                    if (authenticated) {
                        // Dynamically logs role-specific login (e.g., STUDENT_LOGIN, ADMIN_LOGIN, etc.)
                        String actionType = role + "_LOGIN";
                        ActivityLogDao.logAction(userId, actionType, role + " " + username + " (ID: " + userId + ") logged into the system.");
                        return role;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Login Error: " + e.getMessage());
        }
        return null;
    }

    public void logLogout(int userId, String username, String role) {
        if (userId > 0 && username != null && role != null) {
            String cleanRole = role.toUpperCase();
            // Dynamically logs role-specific logout (e.g., STUDENT_LOGOUT, ADMIN_LOGOUT, etc.)
            String actionType = cleanRole + "_LOGOUT";
            ActivityLogDao.logAction(userId, actionType, cleanRole + " " + username + " (ID: " + userId + ") logged out from the system.");
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

    // Deliverable 5 Integration: USER_APPROVED log action
    public boolean approveUser(int adminId, int targetUserId) {
        String sql = "UPDATE users SET status = 'ACTIVE' WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, targetUserId);
            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                ActivityLogDao.logAction(adminId, "USER_APPROVED", "Admin " + adminId + " approved account for User ID " + targetUserId);
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error approving user: " + e.getMessage());
            return false;
        }
    }

    // Deliverable 7 Integration: USER_UPDATED log action
    public boolean updateUserRoleAndStatus(int adminId, int targetUserId, String newRole, String newStatus) {
        String sql = "UPDATE users SET role = ?, status = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newRole.toUpperCase());
            pstmt.setString(2, newStatus.toUpperCase());
            pstmt.setInt(3, targetUserId);
            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                ActivityLogDao.logAction(adminId, "USER_UPDATED", "Admin " + adminId + " changed role of User " + targetUserId + " to " + newRole.toUpperCase());
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }
}