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
        String sql = "INSERT INTO users (username, password, role, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role.toUpperCase());
            pstmt.setString(4, status != null ? status : "PENDING");

            pstmt.executeUpdate();
            System.out.println("-> Success Registration, waiting for Activation!");
            return true;

        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed")) {
                System.err.println("Registration Failed: The username '" + username + "' is already taken.");
            } else {
                System.err.println("Database Error during Registration: " + e.getMessage());
            }
            return false;
        }
    }

    public String loginUser(String username, String password) {
        String sql = "SELECT role FROM users WHERE username = ? AND password = ? AND (status = 'ACTIVE')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role");
                }
            }

        } catch (SQLException e) {
            System.out.println("Login Error: " + e.getMessage());
        }

        return null;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, password, role, status FROM users";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
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
        String sql = "SELECT user_id, username, password, role, status FROM users WHERE status = 'PENDING'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
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
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error approving user: " + e.getMessage());
            return false;
        }
    }

    // --- STUDENT CRUD METHODS (Records to both users and Student tables) ---

    public boolean createStudent(String username, String password, String name, String course, int yearLevel, String contactNumber) {
        String insertUserSql = "INSERT INTO users (username, password, role, status) VALUES (?, ?, 'STUDENT', 'ACTIVE')";
        String insertStudentSql = "INSERT INTO Student (student_name, course, year_level, contact_number, status) VALUES (?, ?, ?, ?, 'ACTIVE')";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement userStmt = conn.prepareStatement(insertUserSql);
                 PreparedStatement studentStmt = conn.prepareStatement(insertStudentSql)) {

                // 1. Insert login account into users table
                userStmt.setString(1, username);
                userStmt.setString(2, password);
                userStmt.executeUpdate();

                // 2. Insert profile details into Student table
                studentStmt.setString(1, name);
                studentStmt.setString(2, course);
                studentStmt.setInt(3, yearLevel);
                studentStmt.setString(4, contactNumber);
                studentStmt.executeUpdate();

                conn.commit(); // Commit both insertions successfully
                return true;

            } catch (SQLException e) {
                conn.rollback(); // Rollback if any error occurs
                if (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed")) {
                    System.err.println("Enrollment Failed: The username '" + username + "' is already taken.");
                } else {
                    System.err.println("Error enrolling student: " + e.getMessage());
                }
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

    public boolean createStudentSchoolAccount(String schoolId, String username, String password, String name, String schoolEmail, String course, int year, String contact) {
        String userSql = "INSERT INTO users (username, password, role, status) VALUES (?, ?, 'STUDENT', 'PENDING')";
        String studentSql = "INSERT INTO Student (school_id, student_name, school_email, course, year_level, contact_number) VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction to keep both inserts atomic

            // 1. Insert into the users table (for system portal login)
            try (PreparedStatement userStmt = conn.prepareStatement(userSql)) {
                userStmt.setString(1, username);
                userStmt.setString(2, password);
                userStmt.executeUpdate();
            }

            // 2. Insert into the Student table (with school ID, email, and details)
            try (PreparedStatement studentStmt = conn.prepareStatement(studentSql)) {
                studentStmt.setString(1, schoolId);
                studentStmt.setString(2, name);
                studentStmt.setString(3, schoolEmail);
                studentStmt.setString(4, course);
                studentStmt.setInt(5, year);
                studentStmt.setString(6, contact);
                studentStmt.executeUpdate();
            }

            conn.commit(); // Commit transaction if both queries succeed
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback changes if an error occurs
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            System.err.println("Error creating student school account: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }
}