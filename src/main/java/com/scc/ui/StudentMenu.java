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
            System.out.println("==========================================================================");
            System.out.print("Press Enter to return...");
            sc.nextLine();
            return;
        }

        while (!back) {
            System.out.println("\n=======================================");
            System.out.println("             STUDENT DASHBOARD         ");
            System.out.println("=========================================");
            System.out.println("1. View Available Services");
            System.out.println("2. Generate / Join Queue");
            System.out.println("3. View My Queue Status & Wait Time");
            System.out.println("4. Logout");
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
                    System.out.println("Invalid choice.");
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
            if (!hasRows) System.out.println(" No active services available.");
            System.out.println("==========================================================================");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void generateQueueTicket(Scanner sc, int studentId) {
        viewServices();
        System.out.print("Enter Service ID to queue for (0 to cancel): ");
        int serviceId;
        try {
            serviceId = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            return;
        }

        if (serviceId == 0) return;

        String queueNumber = "Q-" + (int)(Math.random() * 900 + 100);
        String sql = "INSERT INTO Queue (queue_number, student_id, service_id, queue_date, queue_time, status) VALUES (?, ?, ?, ?, ?, 'WAITING')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, queueNumber);
            pstmt.setInt(2, studentId);
            pstmt.setInt(3, serviceId);
            pstmt.setString(4, LocalDate.now().toString());
            pstmt.setString(5, LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

            pstmt.executeUpdate();
            System.out.println("-> Success! Queue Ticket Generated: [" + queueNumber + "]");
        } catch (SQLException e) {
            System.err.println("Failed to generate ticket: " + e.getMessage());
        }
    }

    private static void viewMyQueueStatus(int studentId) {
        System.out.println("\n=========================================================================================");
        System.out.println("                         MY QUEUE STATUS & ESTIMATED WAIT TIME                           ");
        System.out.println("=========================================================================================");

        String sql = "SELECT q.queue_id, q.queue_number, sv.service_name, q.queue_date, q.queue_time, q.status " +
                "FROM Queue q JOIN Services sv ON q.service_id = sv.service_id WHERE q.student_id = ? ORDER BY q.queue_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("QUEUE #\t\tSERVICE\t\tDATE\t\tTIME\t\tSTATUS\t\tEST. WAIT");
                System.out.println("-----------------------------------------------------------------------------------------");
                boolean hasRows = false;
                while (rs.next()) {
                    hasRows = true;
                    String status = rs.getString("status");
                    String estWait = "N/A";

                    if ("WAITING".equalsIgnoreCase(status)) {
                        // Calculate position ahead in queue for unique wait time estimation (~4 mins per ticket)
                        int peopleAhead = getPeopleAheadCount(conn, rs.getInt("queue_id"));
                        int mins = (peopleAhead + 1) * 4;
                        estWait = "~" + mins + " mins";
                    } else if ("SERVING".equalsIgnoreCase(status)) {
                        estWait = "Serving Now!";
                    }

                    System.out.println(
                            rs.getString("queue_number") + "\t\t" +
                                    rs.getString("service_name") + "\t" +
                                    rs.getString("queue_date") + "\t" +
                                    rs.getString("queue_time") + "\t" +
                                    status + "\t\t" +
                                    estWait
                    );
                }
                if (!hasRows) System.out.println(" No queue history found.");
                System.out.println("=========================================================================================");
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static int getPeopleAheadCount(Connection conn, int queueId) {
        String sql = "SELECT COUNT(*) AS ahead FROM Queue WHERE status = 'WAITING' AND queue_id < ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, queueId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("ahead");
            }
        } catch (SQLException e) {
            // ignore
        }
        return 0;
    }

    private static int getStudentIdByUsername(String username) {
        String sql = "SELECT student_id FROM Student LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt("student_id");
        } catch (SQLException e) {
            // ignore
        }
        return 1;
    }
}