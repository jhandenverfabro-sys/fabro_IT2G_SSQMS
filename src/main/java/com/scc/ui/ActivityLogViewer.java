package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import java.sql.*;
import java.util.Scanner;

public class ActivityLogViewer {

    public static void showLogViewer(Scanner sc, ActivityLogDao logDao) {
        boolean viewing = true;

        while (viewing) {
            System.out.println("\n=======================================================");
            System.out.println("                 SYSTEM ACTIVITY LOGS                  ");
            System.out.println("=======================================================");
            System.out.println("[1] View All Logs");
            System.out.println("[2] Filter Logs by User ID");
            System.out.println("[3] Return to Main Menu");
            System.out.print("Choose an option: ");

            // Input safety: Prevent crashes on non-numeric inputs[cite: 2]
            String input = sc.nextLine();
            int choice;
            try {
                choice = Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid input. Please enter a valid number (1-3).");
                continue; // Re-prompts safely without crashing[cite: 2]
            }

            switch (choice) {
                case 1:
                    printLogsFromDatabase("SELECT log_id, user_id, category, message FROM activity_logs", -1);
                    break;
                case 2:
                    System.out.print("Enter User ID to filter: ");
                    String idInput = sc.nextLine();
                    try {
                        int userId = Integer.parseInt(idInput.trim());
                        printLogsFromDatabase("SELECT log_id, user_id, category, message FROM activity_logs WHERE user_id = ?", userId);
                    } catch (NumberFormatException e) {
                        System.out.println("[!] Invalid User ID format. Returning to log menu.");
                    }
                    break;
                case 3:
                    viewing = false;
                    break;
                default:
                    System.out.println("[!] Choice out of range. Try again.");
            }
        }
    }

    // Formatted table using printf string formatters[cite: 2]
    private static void printLogsFromDatabase(String sql, int filterUserId) {
        System.out.println("\n+--------+---------+--------------------+---------------------------------------------------+");
        System.out.printf("| %-6s | %-7s | %-18s | %-49s |\n", "Log ID", "User ID", "Category", "Description");
        System.out.println("+--------+---------+--------------------+---------------------------------------------------+");

        boolean hasRecords = false;

        try (Connection conn = com.scc.dao.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (filterUserId != -1) {
                pstmt.setInt(1, filterUserId);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    hasRecords = true;
                    int logId = rs.getInt("log_id");
                    int userId = rs.getInt("user_id");
                    String category = rs.getString("category");
                    String message = rs.getString("message");

                    System.out.printf("| %-6d | %-7d | %-18s | %-49s |\n",
                            logId, userId, category, message);
                }
            }
        } catch (SQLException e) {
            System.out.println("[!] Error fetching logs from database: " + e.getMessage());
        }

        if (!hasRecords) {
            System.out.printf("| %-81s |\n", "No log entries found.");
        }
        System.out.println("+--------+---------+--------------------+---------------------------------------------------+");
    }
}