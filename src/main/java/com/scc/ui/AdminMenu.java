package com.scc.ui;

import com.scc.Model.User;
import com.scc.dao.DatabaseConnection;
import com.scc.dao.userDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    public static void showMenu(Scanner sc, userDao userDao, User currentAdmin) {
        boolean back = false;

        while (!back) {
            System.out.println("\n=======================================");
            System.out.println("            ADMINISTRATOR MENU         ");
            System.out.println("=========================================");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Update User Role & Status");
            System.out.println("4. Approve User Account");
            System.out.println("5. Manage Verified Students");
            System.out.println("6. Logout");
            System.out.print("Choose an option: ");

            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    printUserTable(userDao.getAllUsers(), "ALL SYSTEM USERS");
                    break;
                case "2":
                    printUserTable(userDao.getPendingUsers(), "PENDING ACCOUNT REGISTRATIONS");
                    break;
                case "3":
                    handleUserRoleAndStatusUpdate(sc, userDao, currentAdmin);
                    break;
                case "4":
                    handleUserApproval(sc, userDao);
                    break;
                case "5":
                    manageStudentsMenu(sc, userDao);
                    break;
                case "6":
                    back = true;
                    System.out.println("Logging out from Admin menu...");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static int readIntInput(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid number (or 0 to cancel).");
            }
        }
    }

    private static String readValidRole(Scanner sc) {
        while (true) {
            System.out.print("Enter New Role (STUDENT, STAFF, SUPERVISOR, ADMIN): ");
            String role = sc.nextLine().trim().toUpperCase();
            if (role.equals("STUDENT") || role.equals("STAFF") || role.equals("SUPERVISOR") || role.equals("ADMIN")) {
                return role;
            }
            System.out.println("Invalid role!");
        }
    }

    private static String readValidStatus(Scanner sc) {
        while (true) {
            System.out.print("Enter New Status (ACTIVE, PENDING): ");
            String status = sc.nextLine().trim().toUpperCase();
            if (status.equals("ACTIVE") || status.equals("PENDING")) {
                return status;
            }
            System.out.println("Invalid status!");
        }
    }

    private static void handleUserRoleAndStatusUpdate(Scanner sc, userDao userDao, User currentAdmin) {
        printUserTable(userDao.getAllUsers(), "ALL SYSTEM USERS");
        int targetId = readIntInput(sc, "Enter Target User ID to Update (or 0 to cancel): ");
        if (targetId == 0) return;

        if (targetId == currentAdmin.id()) {
            System.out.println("WARNING: Modifying your own active admin account is restricted.");
            return;
        }

        String newRole = readValidRole(sc);
        String newStatus = readValidStatus(sc);

        if (userDao.updateUserRoleAndStatus(targetId, newRole, newStatus)) {
            System.out.println("-> Success! User ID " + targetId + " updated.");
        } else {
            System.out.println("-> Failed to update user.");
        }
    }

    private static void handleUserApproval(Scanner sc, userDao userDao) {
        List<User> pending = userDao.getPendingUsers();
        printUserTable(pending, "PENDING ACCOUNTS AVAILABLE FOR APPROVAL");

        if (pending.isEmpty()) return;

        int targetId = readIntInput(sc, "Enter Target User ID to Approve (or enter 0 to cancel): ");
        if (targetId == 0) return;

        boolean exists = pending.stream().anyMatch(u -> u.id() == targetId);
        if (exists) {
            if (userDao.approveUser(targetId)) {
                System.out.println("-> Success! User ID " + targetId + " status updated to 'ACTIVE'.");
            } else {
                System.out.println("-> Failed to approve user.");
            }
        } else {
            System.out.println("-> Error: User ID not found in pending list.");
        }
    }

    public static void manageStudentsMenu(Scanner sc, userDao userDao) {
        boolean back = false;
        while (!back) {
            System.out.println("\n=======================================");
            System.out.println("          STUDENT MANAGEMENT (CRUD)    ");
            System.out.println("=======================================");
            System.out.println("1. Enrol New Student");
            System.out.println("2. View Student Directory");
            System.out.println("3. Update Student Record");
            System.out.println("4. Delete Student Record");
            System.out.println("5. Back");
            System.out.print("Choose an option: ");

            String choice = sc.nextLine();
            switch (choice) {
                case "1":
                    System.out.print("Enter Student Username: ");
                    String username = sc.nextLine();
                    System.out.print("Enter Password: ");
                    String password = sc.nextLine();
                    System.out.print("Enter Full Name: ");
                    String name = sc.nextLine();
                    System.out.print("Enter Course: ");
                    String course = sc.nextLine();
                    int year = readIntInput(sc, "Enter Year Level: ");
                    System.out.print("Enter Contact Number: ");
                    String contact = sc.nextLine();

                    if (userDao.createStudent(username, password, name, course, year, contact)) {
                        System.out.println("-> Success! Student created and enrolled.");
                    } else {
                        System.out.println("-> Enrollment failed.");
                    }
                    break;
                case "2":
                    viewStudentDirectory();
                    break;
                case "3":
                    viewStudentDirectory();
                    int updateId = readIntInput(sc, "Enter Student ID to Update: ");
                    System.out.print("Enter New Full Name: ");
                    String newName = sc.nextLine();
                    System.out.print("Enter New Course: ");
                    String newCourse = sc.nextLine();
                    int newYear = readIntInput(sc, "Enter New Year Level: ");
                    System.out.print("Enter New Contact Number: ");
                    String newContact = sc.nextLine();

                    if (userDao.updateStudent(updateId, newName, newCourse, newYear, newContact)) {
                        System.out.println("-> Success! Student updated.");
                    } else {
                        System.out.println("-> Update failed.");
                    }
                    break;
                case "4":
                    viewStudentDirectory();
                    int deleteId = readIntInput(sc, "Enter Student ID to Delete (0 to cancel): ");
                    if (deleteId != 0 && userDao.deleteStudent(deleteId)) {
                        System.out.println("-> Success! Student deleted.");
                    } else {
                        System.out.println("-> Deletion failed.");
                    }
                    break;
                case "5":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    public static void viewStudentDirectory() {
        System.out.println("\n====================================================================================");
        System.out.println("                              VERIFIED STUDENT DIRECTORY                            ");
        System.out.println("====================================================================================");

        String sql = "SELECT student_id, student_name, course, year_level, contact_number, status FROM Student";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("ID\tNAME\t\t\tCOURSE\t\tYEAR\tCONTACT\t\tSTATUS");
            System.out.println("------------------------------------------------------------------------------------");
            while (rs.next()) {
                System.out.println(
                        rs.getString("student_id") + "\t" +
                                rs.getString("student_name") + "\t\t" +
                                rs.getString("course") + "\t\t" +
                                rs.getInt("year_level") + "\t" +
                                rs.getString("contact_number") + "\t" +
                                rs.getString("status")
                );
            }
            System.out.println("====================================================================================");
        } catch (SQLException e) {
            System.err.println("Error fetching student directory: " + e.getMessage());
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
}