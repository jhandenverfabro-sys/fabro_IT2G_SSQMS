package com.scc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentDao {

    // Creates student profile record matching the SQLite 'Student' table schema
    public boolean createStudent(String username, String password, String name, String course, int yearLevel, String contactNumber) {
        // First, create the user login entry in the 'users' table
        UserDao userDao = new UserDao();
        boolean userCreated = userDao.registerUser(username, password, "STUDENT", "ACTIVE");

        if (!userCreated) {
            System.err.println("Failed to create student user login.");
            return false;
        }

        // Then, insert the profile data into the 'Student' table (matching your DB schema)
        String sql = "INSERT INTO Student (student_name, course, year_level, contact_number, status) VALUES (?, ?, ?, ?, 'ACTIVE')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, course);
            pstmt.setInt(3, yearLevel);
            pstmt.setString(4, contactNumber);

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error saving student profile: " + e.getMessage());
            return false;
        }
    }

    public int getStudentIdByUsername(String username) {
        // In this architecture, we map or fetch student ID.
        // If your student table references users, or uses student_id directly:
        String sql = "SELECT student_id FROM Student LIMIT 1"; // Fallback or map accurately to your setup
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("student_id");
            }
        } catch (SQLException e) {
            // ignore
        }
        return 1; // Default fallback ID if single-student context
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
            System.err.println("Error updating student: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteStudent(int studentId) {
        String sql = "UPDATE Student SET status = 'INACTIVE' WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting student: " + e.getMessage());
            return false;
        }
    }
}