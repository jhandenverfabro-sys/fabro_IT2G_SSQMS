package com.scc.dao;

import com.scc.Model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class userDao {

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
            System.out.println("-> Registration successful! Account is pending approval.");
            return true;
        } catch (SQLException e) {
            System.err.println("Registration failed (Username might already exist): " + e.getMessage());
            return false;
        }
    }

    // --- Atomic Staff/Supervisor Registration (Inserts to both users & Staff tables) ---
    public boolean registerStaff(String staffName, String username, String password, String role) {
        String salt = SecurityUtil.generateSalt();
        String hashedPassword = SecurityUtil.hashPassword(password, salt);
        String secureStoredValue = salt + ":" + hashedPassword;

        String insertUserSql = "INSERT INTO users (username, password, role, status) VALUES (?, ?, ?, 'PENDING')";
        String insertStaffSql = "INSERT INTO Staff (staff_name, username, password, role, status) VALUES (?, ?, ?, ?, 'ACTIVE')";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Begin transaction

            try (PreparedStatement userStmt = conn.prepareStatement(insertUserSql);
                 PreparedStatement staffStmt = conn.prepareStatement(insertStaffSql)) {

                // 1. Insert into users table for portal authentication
                userStmt.setString(1, username);
                userStmt.setString(2, secureStoredValue);
                userStmt.setString(3, role.toUpperCase());
                userStmt.executeUpdate();

                // 2. Insert into Staff table directory profile automatically
                staffStmt.setString(1, staffName);
                staffStmt.setString(2, username);
                staffStmt.setString(3, secureStoredValue);
                staffStmt.setString(4, role.toUpperCase());
                staffStmt.executeUpdate();

                conn.commit(); // Commit both queries successfully
                System.out.println("-> Success! Staff account registered in 'users' and added to 'Staff' table.");
                return true;

            } catch (SQLException e) {
                conn.rollback(); // Rollback if any error occurs
                System.err.println("Staff Registration Failed: " + e.getMessage());
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateUserRoleAndStatus(int userId, String newRole, String newStatus) {
        String sql = "UPDATE users SET role = ?, status = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newRole.toUpperCase());
            pstmt.setString(2, newStatus.toUpperCase());
            pstmt.setInt(3, userId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user role and status: " + e.getMessage());
            return false;
        }
    }

    public String loginUser(String username, String password) {
        String sql = "SELECT password, role FROM users WHERE username = ? AND status = 'ACTIVE'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedCredentials = rs.getString("password");
                    String role = rs.getString("role");

                    if (storedCredentials.contains(":")) {
                        String[] parts = storedCredentials.split(":");
                        String salt = parts[0];
                        String hash = parts[1];

                        if (SecurityUtil.verifyPassword(password, salt, hash)) {
                            return role;
                        }
                    } else if (storedCredentials.equals(password)) {
                        return role; // Fallback for legacy unhashed entries
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Login Error: " + e.getMessage());
        }
        return null;
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
            System.err.println("Error fetching all users: " + e.getMessage());
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

    public boolean approveUser(int userId) {
        String sql = "UPDATE users SET status = 'ACTIVE' WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error approving user: " + e.getMessage());
            return false;
        }
    }

    public boolean createStudent(String username, String password, String name, String course, int yearLevel, String contactNumber) {
        String salt = SecurityUtil.generateSalt();
        String hashedPassword = SecurityUtil.hashPassword(password, salt);
        String secureStoredValue = salt + ":" + hashedPassword;

        String insertUserSql = "INSERT INTO users (username, password, role, status) VALUES (?, ?, 'STUDENT', 'ACTIVE')";
        String insertStudentSql = "INSERT INTO Student (student_name, course, year_level, contact_number, status) VALUES (?, ?, ?, ?, 'ACTIVE')";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement userStmt = conn.prepareStatement(insertUserSql);
                 PreparedStatement studentStmt = conn.prepareStatement(insertStudentSql)) {

                userStmt.setString(1, username);
                userStmt.setString(2, secureStoredValue);
                userStmt.executeUpdate();

                studentStmt.setString(1, name);
                studentStmt.setString(2, course);
                studentStmt.setInt(3, yearLevel);
                studentStmt.setString(4, contactNumber);
                studentStmt.executeUpdate();

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Error enrolling student: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateStudent(int studentId, String name, String course, int yearLevel, String contactNumber) {
        String sql = "UPDATE Student SET student_name = ?, course = ?, year_level = ?, contact_number = ? WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, course);
            pstmt.setInt(3, yearLevel);
            pstmt.setString(4, contactNumber);
            pstmt.setInt(5, studentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating student record: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteStudent(int studentId) {
        String sql = "DELETE FROM Student WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting student record: " + e.getMessage());
            return false;
        }
    }
}