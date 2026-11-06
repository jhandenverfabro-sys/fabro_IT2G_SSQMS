package com.scc.ui;

import com.scc.Model.ActivityLog;
import com.scc.dao.ActivityLogDao;
import java.util.List;
import java.util.Scanner;

public class ActivityLogViewer {

    public static void showLogViewer(Scanner sc, ActivityLogDao logDao) {
        boolean viewing = true;

        while (viewing) {
            System.out.println("\n=======================================================");
            System.out.println("            VIEW SYSTEM AUDIT LOGS                     ");
            System.out.println("=======================================================");
            System.out.println("[1] View All System Logs");
            System.out.println("[2] Filter Logs by User ID");
            System.out.println("[3] Return to Admin Menu");
            System.out.print("Choose an option: ");

            String input = sc.nextLine();
            int choice;
            try {
                choice = Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid input. Please enter a valid number (1-3).");
                continue;
            }

            switch (choice) {
                case 1:
                    List<ActivityLog> allLogs = logDao.getAllLogs();
                    printLogTable(allLogs);
                    break;
                case 2:
                    System.out.print("Enter User ID to filter: ");
                    String idInput = sc.nextLine();
                    try {
                        int userId = Integer.parseInt(idInput.trim());
                        List<ActivityLog> userLogs = logDao.getLogsByUserId(userId);
                        printLogTable(userLogs);
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

    // Exact required printf format: `%-5s | %-8s | %-18s | %-35s | %-20s`
    private static void printLogTable(List<ActivityLog> logs) {
        System.out.println("\n+-------+----------+--------------------+-------------------------------------+----------------------+");
        System.out.printf("| %-5s | %-8s | %-18s | %-35s | %-20s |\n", "ID", "User ID", "Action Type", "Description", "Timestamp");
        System.out.println("+-------+----------+--------------------+-------------------------------------+----------------------+");

        if (logs == null || logs.isEmpty()) {
            System.out.printf("| %-95s |\n", "No log entries found.");
        } else {
            for (ActivityLog log : logs) {
                System.out.printf("| %-5d | %-8d | %-18s | %-35s | %-20s |\n",
                        log.getLogId(),
                        log.getUserId(),
                        log.getActionType(),
                        log.getDescription(),
                        log.getTimestamp());
            }
        }
        System.out.println("+-------+----------+--------------------+-------------------------------------+----------------------+");
    }
}