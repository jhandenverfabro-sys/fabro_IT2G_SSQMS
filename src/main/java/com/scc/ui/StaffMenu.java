package com.scc.ui;

import com.scc.dao.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class StaffMenu {

    public static void showMenu(Scanner sc, String staffUsername) {
        boolean back = false;
        int staffId = getStaffIdByUsername(staffUsername);

        while (!back) {
            System.out.println("\n=======================================");
            System.out.println("             STAFF DASHBOARD           ");
            System.out.println("=========================================");
            System.out.println("1. View Waiting Queue");
            System.out.println("2. Call / Process Next Student");
            System.out.println("3. View Student Directory");
            System.out.println("4. Logout / Back");
            System.out.print("Choose an option: ");

            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    viewWaitingQueue();
                    break;

                case "2":
                    processNextQueue(sc, staffId);
                    break;

                case "3":
                    viewStudentDirectory();
                    break;

                case "4":
                    back = true;
                    System.out.println("Logging out from Staff menu...");
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void viewWaitingQueue() {
        System.out.println("\n==========================================================================");
        System.out.println("                        CURRENT WAITING QUEUE                             ");
        System.out.println("==========================================================================");

        String sql = "SELECT q.queue_id, q.queue_number, s.student_name, sv.service_name, q.queue_time, q.status " +
                "FROM Queue q " +
                "JOIN Student s ON q.student_id = s.student_id " +
                "JOIN Services sv ON q.service_id = sv.service_id " +
                "WHERE q.status = 'WAITING' " +
                "ORDER BY q.queue_id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("QUEUE ID\tQUEUE #\t\tSTUDENT NAME\t\tSERVICE\t\tTIME");
            System.out.println("--------------------------------------------------------------------------");

            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                System.out.println(
                        rs.getString("queue_id") + "\t\t" +
                                rs.getString("queue_number") + "\t\t" +
                                rs.getString("student_name") + "\t\t" +
                                rs.getString("service_name") + "\t" +
                                rs.getString("queue_time")
                );
            }

            if (!hasRows) {
                System.out.println(" No students currently waiting in the queue.");
            }
            System.out.println("==========================================================================");

        } catch (SQLException e) {
            System.err.println("Error fetching waiting queue: " + e.getMessage());
        }
    }

    private static void processNextQueue(Scanner sc, int staffId) {
        viewWaitingQueue();

        System.out.print("Enter Queue ID to mark as SERVED (or enter 0 to cancel): ");
        String input = sc.nextLine();

        int queueId;
        try {
            queueId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input! Operation cancelled.");
            return;
        }

        if (queueId == 0) {
            System.out.println("Action cancelled.");
            return;
        }

        String updateQueueSql = "UPDATE Queue SET status = 'SERVED' WHERE queue_id = ? AND status = 'WAITING'";
        String logActivitySql = "INSERT INTO QueueActivityLog (queue_id, staff_id, action, date, time) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement updateStmt = conn.prepareStatement(updateQueueSql);
                 PreparedStatement logStmt = conn.prepareStatement(logActivitySql)) {

                updateStmt.setInt(1, queueId);
                int rowsAffected = updateStmt.executeUpdate();

                if (rowsAffected > 0) {
                    String currentDate = LocalDate.now().toString();
                    String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

                    logStmt.setInt(1, queueId);
                    logStmt.setInt(2, staffId);
                    logStmt.setString(3, "SERVED");
                    logStmt.setString(4, currentDate);
                    logStmt.setString(5, currentTime);
                    logStmt.executeUpdate();

                    conn.commit();
                    System.out.println("-> Success! Queue ID " + queueId + " marked as SERVED and logged.");
                } else {
                    conn.rollback();
                    System.out.println("-> Error: Queue ID not found or already processed.");
                }

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.err.println("Database error during queue processing: " + e.getMessage());
        }
    }

    private static void viewStudentDirectory() {
        System.out.println("\n==========================================================================");
        System.out.println("                         STUDENT DIRECTORY                                ");
        System.out.println("==========================================================================");

        String sql = "SELECT student_id, student_name, course, year_level, contact_number, status FROM Student";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("ID\tNAME\t\t\tCOURSE\t\tYEAR\tCONTACT");
            System.out.println("--------------------------------------------------------------------------");

            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                System.out.println(
                        rs.getString("student_id") + "\t" +
                                rs.getString("student_name") + "\t\t" +
                                rs.getString("course") + "\t\t" +
                                rs.getString("year_level") + "\t" +
                                rs.getString("contact_number")
                );
            }

            if (!hasRows) {
                System.out.println(" No student records found.");
            }
            System.out.println("==========================================================================");

        } catch (SQLException e) {
            System.err.println("Error fetching student directory: " + e.getMessage());
        }
    }

    private static int getStaffIdByUsername(String username) {
        String sql = "SELECT staff_id FROM Staff WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("staff_id");
                }
            }
        } catch (SQLException e) {
            // Fallback default ID
        }
        return 1;
    }
}