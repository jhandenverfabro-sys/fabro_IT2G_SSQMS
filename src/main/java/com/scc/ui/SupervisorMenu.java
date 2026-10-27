package com.scc.ui;

import com.scc.dao.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class SupervisorMenu {

    public static void showMenu(Scanner sc, String supervisorUsername) {
        boolean back = false;

        while (!back) {
            System.out.println("\n=======================================");
            System.out.println("          SUPERVISOR DASHBOARD         ");
            System.out.println("=========================================");
            System.out.println("1. View All Queue Activity Logs");
            System.out.println("2. View Staff Performance Summary");
            System.out.println("3. View Overall System Queue Summary");
            System.out.println("4. Logout");
            System.out.print("Choose an option: ");

            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    viewActivityLogs();
                    break;
                case "2":
                    viewStaffPerformance();
                    break;
                case "3":
                    viewQueueSummary();
                    break;
                case "4":
                    back = true;
                    System.out.println("Logging out from Supervisor menu...");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void viewActivityLogs() {
        System.out.println("\n==========================================================================");
        System.out.println("                         QUEUE ACTIVITY AUDIT LOGS                        ");
        System.out.println("==========================================================================");

        String sql = "SELECT l.log_id, q.queue_number, st.staff_name, l.action, l.date, l.time " +
                "FROM QueueActivityLog l " +
                "JOIN Queue q ON l.queue_id = q.queue_id " +
                "JOIN Staff st ON l.staff_id = st.staff_id " +
                "ORDER BY l.log_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("LOG ID\tQUEUE #\t\tSTAFF NAME\tACTION\t\tDATE\t\tTIME");
            System.out.println("--------------------------------------------------------------------------");
            while (rs.next()) {
                System.out.println(
                        rs.getInt("log_id") + "\t\t" +
                                rs.getString("queue_number") + "\t\t" +
                                rs.getString("staff_name") + "\t\t" +
                                rs.getString("action") + "\t" +
                                rs.getString("date") + "\t" +
                                rs.getString("time")
                );
            }
            System.out.println("==========================================================================");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void viewStaffPerformance() {
        System.out.println("\n==========================================================================");
        System.out.println("                         STAFF PERFORMANCE SUMMARY                        ");
        System.out.println("==========================================================================");

        String sql = "SELECT st.staff_id, st.staff_name, st.role, COUNT(l.log_id) AS total_handled " +
                "FROM Staff st LEFT JOIN QueueActivityLog l ON st.staff_id = l.staff_id GROUP BY st.staff_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("STAFF ID\tSTAFF NAME\t\tROLE\t\tTASKS PROCESSED");
            System.out.println("--------------------------------------------------------------------------");
            while (rs.next()) {
                System.out.println(
                        rs.getInt("staff_id") + "\t\t" +
                                rs.getString("staff_name") + "\t\t" +
                                rs.getString("role") + "\t\t" +
                                rs.getInt("total_handled")
                );
            }
            System.out.println("==========================================================================");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void viewQueueSummary() {
        System.out.println("\n==========================================================================");
        System.out.println("                         OVERALL QUEUE STATUS SUMMARY                     ");
        System.out.println("==========================================================================");

        String sql = "SELECT status, COUNT(*) AS count FROM Queue GROUP BY status";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("QUEUE STATUS\t\tTOTAL TICKETS");
            System.out.println("--------------------------------------------------------------------------");
            while (rs.next()) {
                System.out.println(
                        rs.getString("status") + "\t\t\t" +
                                rs.getInt("count")
                );
            }
            System.out.println("==========================================================================");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}