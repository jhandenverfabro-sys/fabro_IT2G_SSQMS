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
            System.out.println("1. View Activity Audit Logs");
            System.out.println("2. View Overall System Queue Summary");
            System.out.println("3. Logout");
            System.out.print("Choose an option: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    viewActivityLogs();
                    break;
                case "2":
                    viewQueueSummary();
                    break;
                case "3":
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
        System.out.println("                         ACTIVITY AUDIT LOGS                              ");
        System.out.println("==========================================================================");

        // Fixed: Using u.user_id to match your SQLite schema configuration
        String sql = "SELECT l.id, u.username, l.action_type, l.description, l.timestamp " +
                "FROM activity_logs l JOIN users u ON l.user_id = u.user_id ORDER BY l.id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("LOG ID\tUSERNAME\tACTION\t\tDETAILS\t\t\tTIMESTAMP");
            System.out.println("--------------------------------------------------------------------------");
            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                System.out.println(
                        rs.getInt("id") + "\t" +
                                rs.getString("username") + "\t\t" +
                                rs.getString("action_type") + "\t\t" +
                                rs.getString("description") + "\t" +
                                rs.getString("timestamp")
                );
            }
            if (!hasRows) System.out.println(" No audit logs recorded yet.");
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
                        rs.getString("status").toUpperCase() + "\t\t\t" +
                                rs.getInt("count")
                );
            }
            System.out.println("==========================================================================");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}