package com.scc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentDao {

    public boolean createStudent(String username, String password, String name, String course, int yearLevel, String contactNumber) {
        String salt = SecurityUtil.generateSalt();
        String hashedPassword = SecurityUtil.hashPassword(password, salt);
        String secureStoredValue = salt + ":" + hashedPassword;

        String insertUserSql = "INSERT INTO users (username, password, role, status) VALUES (?, ?, 'STUDENT', 'ACTIVE')";
        String insertStudentSql = "INSERT INTO Student (username, student_name, course, year_level, contact_number, status) VALUES (?, ?, ?, ?, ?, 'ACTIVE')";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement userStmt = conn.prepareStatement(insertUserSql);
                 PreparedStatement studentStmt = conn.prepareStatement(insertStudentSql)) {

                userStmt.setString(1, username);
                userStmt.setString(2, secureStoredValue);
                userStmt.executeUpdate();

                studentStmt.setString(1, username);
                studentStmt.setString(2, name);
                studentStmt.setString(3, course);
                studentStmt.setInt(4, yearLevel);
                studentStmt.setString(5, contactNumber);
                studentStmt.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Student creation failed: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            return false;
        }
    }

    public int getStudentIdByUsername(String username) {
        String sql = "SELECT student_id FROM Student WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("student_id");
            }
        } catch (SQLException e) {
            System.err.println("Error finding student ID: " + e.getMessage());
        }
        return -1;
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
        String sql = "DELETE FROM Student WHERE student_id = ?";
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