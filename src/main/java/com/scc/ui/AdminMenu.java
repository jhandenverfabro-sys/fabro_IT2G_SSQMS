package com.scc.ui;

import com.scc.Model.User;
import com.scc.dao.userDao;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    public static void showMenu(Scanner sc, userDao userDao) {
        boolean back = false;

        while (!back) {
            System.out.println("\n=======================================");
            System.out.println("            ADMINISTRATOR MENU         ");
            System.out.println("=========================================");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Logout / Back");
            System.out.print("Choose an option: ");

            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    List<User> allUsers = userDao.getAllUsers();
                    printUserTable(allUsers, "ALL SYSTEM USERS");
                    break;

                case "2":
                    List<User> pendingUsers = userDao.getPendingUsers();
                    printUserTable(pendingUsers, "PENDING ACCOUNT REGISTRATIONS");
                    break;

                case "3":
                    handleUserApproval(sc, userDao);
                    break;

                case "4":
                    back = true;
                    System.out.println("Logging out from Admin menu...");
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void printUserTable(List<User> users, String title) {
        System.out.println("\n======================================================================");
        System.out.println(" " + title);
        System.out.println("======================================================================");

        if (users.isEmpty()) {
            System.out.println(" No records found.");
            System.out.println("======================================================================");
            return;
        }

        System.out.println("ID\tUSERNAME\t\tROLE\t\tSTATUS");
        System.out.println("----------------------------------------------------------------------");

        for (User u : users) {
            System.out.println(u.id() + "\t" + u.username() + "\t\t" + u.role() + "\t\t" + u.status());
        }
        System.out.println("======================================================================");
    }

    private static void handleUserApproval(Scanner sc, userDao userDao) {
        List<User> pending = userDao.getPendingUsers();
        printUserTable(pending, "PENDING ACCOUNTS AVAILABLE FOR APPROVAL");

        if (pending.isEmpty()) {
            return;
        }

        System.out.print("Enter Target User ID to Approve (or enter 0 to cancel): ");
        String input = sc.nextLine();

        int targetId;
        try {
            targetId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input! Operation cancelled.");
            return;
        }

        if (targetId == 0) {
            System.out.println("Approval action cancelled.");
            return;
        }

        boolean existsInPending = pending.stream().anyMatch(u -> u.id() == targetId);

        if (existsInPending) {
            boolean success = userDao.approveUser(targetId);
            if (success) {
                System.out.println("-> Success! User ID " + targetId + " status updated to 'APPROVED'.");
            } else {
                System.out.println("-> Failed to update user status in the database.");
            }
        } else {
            System.out.println("-> Error: User ID " + targetId + " was not found in the pending list.");
        }
    }
}