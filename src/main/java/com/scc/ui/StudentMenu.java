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

public class StudentMenu {

    public static void showMenu(Scanner sc, String studentUsername) {
        boolean back = false;
        int studentId = getStudentIdByUsername(studentUsername);

        if (studentId == -1) {
            System.out.println("\n==========================================================================");
            System.out.println(" Notice: Your student account is not yet verified or enrolled by Admin/Staff.");
            System.out.println(" Please contact school administration to verify and enroll your record.");
            System.out.println("==========================================================================");
            System.out.println("Press Enter to return to main login...");
            sc.nextLine();
            return;
        }

        while (!back) {
            System.out.println("\n=======================================");
            System.out.println("             STUDENT DASHBOARD         ");
            System.out.println("=========================================");
            System.out.println("1. View Available Services");
            System.out.println("2. Generate / Join Queue");
            System.out.println("3. View My Queue Status");
            System.out.println("4. Logout / Back");
            System.out.print("Choose an option: ");

            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    viewServices();
                    break;

                case "2":
                    generateQueueTicket(sc, studentId);
                    break;

                case "3":
                    viewMyQueueStatus(studentId);
                    break;

                case "4":
                    back = true;
                    System.out.println("Logging out from Student menu...");
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void viewServices() {
        System.out.println("\n==========================================================================");
        System.out.println("                         AVAILABLE CAMPUS SERVICES                        ");
        System.out.println("==========================================================================");

        String sql = "SELECT service_id, service_name, description, counter_number, status FROM Services WHERE status = 'ACTIVE'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("ID\tSERVICE NAME\t\tCOUNTER #\tDESCRIPTION");
            System.out.println("--------------------------------------------------------------------------");

            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                System.out.println(
                        rs.getInt("service_id") + "\t" +
                                rs.getString("service_name") + "\t\t" +
                                rs.getInt("counter_number") + "\t\t" +
                                rs.getString("description")
                );
            }

            if (!hasRows) {
                System.out.println(" No active services available at the moment.");
            }
            System.out.println("==========================================================================");

        } catch (SQLException e) {
            System.err.println("Error fetching services: " + e.getMessage());
        }
    }

    private static void generateQueueTicket(Scanner sc, int studentId) {
        viewServices();

        System.out.print("Enter Service ID to queue for (or enter 0 to cancel): ");
        String input = sc.nextLine();

        int serviceId;
        try {
            serviceId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input! Operation cancelled.");
            return;
        }

        if (serviceId == 0) {
            System.out.println("Queue generation cancelled.");
            return;
        }

        String queueNumber = "Q-" + (int)(Math.random() * 900 + 100);
        String queueDate = LocalDate.now().toString();
        String queueTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        String sql = "INSERT INTO Queue (queue_number, student_id, service_id, queue_date, queue_time, status) VALUES (?, ?, ?, ?, ?, 'WAITING')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, queueNumber);
            pstmt.setInt(2, studentId);
            pstmt.setInt(3, serviceId);
            pstmt.setString(4, queueDate);
            pstmt.setString(5, queueTime);

            pstmt.executeUpdate();
            System.out.println("-> Success! Queue Ticket Generated: [" + queueNumber + "] for Date: " + queueDate);

        } catch (SQLException e) {
            System.err.println("Failed to generate queue ticket: " + e.getMessage());
        }
    }

    private static void viewMyQueueStatus(int studentId) {
        System.out.println("\n==========================================================================");
        System.out.println("                         MY QUEUE STATUS HISTORY                          ");
        System.out.println("==========================================================================");

        String sql = "SELECT q.queue_id, q.queue_number, sv.service_name, q.queue_date, q.queue_time, q.status " +
                "FROM Queue q " +
                "JOIN Services sv ON q.service_id = sv.service_id " +
                "WHERE q.student_id = ? " +
                "ORDER BY q.queue_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("QUEUE #\t\tSERVICE\t\tDATE\t\tTIME\t\tSTATUS");
                System.out.println("--------------------------------------------------------------------------");

                boolean hasRows = false;
                while (rs.next()) {
                    hasRows = true;
                    System.out.println(
                            rs.getString("queue_number") + "\t\t" +
                                    rs.getString("service_name") + "\t" +
                                    rs.getString("queue_date") + "\t" +
                                    rs.getString("queue_time") + "\t" +
                                    rs.getString("status")
                    );
                }

                if (!hasRows) {
                    System.out.println(" You have no queue tickets history.");
                }
                System.out.println("==========================================================================");
            }

        } catch (SQLException e) {
            System.err.println("Error fetching queue status: " + e.getMessage());
        }
    }

    private static int getStudentIdByUsername(String username) {
        // Can be linked if username matches student record or table design.
        // For now, looking up active student records.
        String sql = "SELECT student_id FROM Student LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("student_id");
            }
        } catch (SQLException e) {
            // ignore
        }
        return -1;
    }
}