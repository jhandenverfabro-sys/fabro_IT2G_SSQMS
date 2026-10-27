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

    public static void showMenu(Scanner sc, userDao userDao) {
        boolean back = false;

        while (!back) {
            System.out.println("\n=======================================");
            System.out.println("            ADMINISTRATOR MENU         ");
            System.out.println("=========================================");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Manage Verified Students");
            System.out.println("5. Logout");
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
                    manageStudentsMenu(sc, userDao);
                    break;

                case "5":
                    back = true;
                    System.out.println("Logging out from Admin menu...");
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    public static void manageStudentsMenu(Scanner sc, userDao userDao) {
        boolean studentMenuBack = false;

        while (!studentMenuBack) {
            System.out.println("\n=======================================");
            System.out.println("          STUDENT MANAGEMENT (CRUD)    ");
            System.out.println("=========================================");
            System.out.println("1. Enrol New Student");
            System.out.println("2. View Student Directory");
            System.out.println("3. Update Student Record");
            System.out.println("4. Delete Student Record");
            System.out.println("5. Back to Main Menu");
            System.out.print("Choose an option: ");

            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Enter Student Username (for login): ");
                    String username = sc.nextLine();
                    System.out.print("Enter Student Password: ");
                    String password = sc.nextLine();
                    System.out.print("Enter Student Full Name: ");
                    String name = sc.nextLine();
                    System.out.print("Enter Course (e.g. BSIT): ");
                    String course = sc.nextLine();
                    System.out.print("Enter Year Level (e.g. 2): ");
                    int year = Integer.parseInt(sc.nextLine());
                    System.out.print("Enter Contact Number: ");
                    String contact = sc.nextLine();

                    boolean created = userDao.createStudent(username, password, name, course, year, contact);
                    if (created) {
                        System.out.println("-> Success! Student account created in 'users' and enrolled in 'Student' table.");
                    } else {
                        System.out.println("-> Failed to enrol student.");
                    }
                    break;

                case "2":
                    viewStudentDirectory();
                    break;

                case "3":
                    viewStudentDirectory();
                    System.out.print("Enter Student ID to Update: ");
                    int updateId = Integer.parseInt(sc.nextLine());
                    System.out.print("Enter New Full Name: ");
                    String newName = sc.nextLine();
                    System.out.print("Enter New Course: ");
                    String newCourse = sc.nextLine();
                    System.out.print("Enter New Year Level: ");
                    int newYear = Integer.parseInt(sc.nextLine());
                    System.out.print("Enter New Contact Number: ");
                    String newContact = sc.nextLine();

                    boolean updated = userDao.updateStudent(updateId, newName, newCourse, newYear, newContact);
                    if (updated) {
                        System.out.println("-> Success! Student ID " + updateId + " updated.");
                    } else {
                        System.out.println("-> Update failed. Student ID not found.");
                    }
                    break;

                case "4":
                    viewStudentDirectory();
                    System.out.print("Enter Student ID to Delete (or 0 to cancel): ");
                    int deleteId = Integer.parseInt(sc.nextLine());
                    if (deleteId != 0) {
                        boolean deleted = userDao.deleteStudent(deleteId);
                        if (deleted) {
                            System.out.println("-> Success! Student ID " + deleteId + " deleted from database.");
                        } else {
                            System.out.println("-> Deletion failed. Student ID not found.");
                        }
                    }
                    break;

                case "5":
                    studentMenuBack = true;
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
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

            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                System.out.println(
                        rs.getString("student_id") + "\t" +
                                rs.getString("student_name") + "\t\t" +
                                rs.getString("course") + "\t\t" +
                                rs.getString("year_level") + "\t" +
                                rs.getString("contact_number") + "\t" +
                                rs.getString("status")
                );
            }

            if (!hasRows) {
                System.out.println(" No student records found.");
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