package com.scc.ui;

import com.scc.dao.DatabaseConnection;
import com.scc.dao.userDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class StaffMenu {
    private static final userDao userDao = new userDao();

    public static void showMenu(Scanner sc, String staffUsername) {
        boolean back = false;
        int staffId = getStaffIdByUsername(staffUsername);

        while (!back) {
            System.out.println("\n=======================================");
            System.out.println("             STAFF DASHBOARD           ");
            System.out.println("=========================================");
            System.out.println("1. View Waiting Queue");
            System.out.println("2. Call / Process Next Student");
            System.out.println("3. Manage Queue");
            System.out.println("4. Manage Student Services");
            System.out.println("5. Manage Verified Students");
            System.out.println("6. Logout");
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
                    manageQueueMenu(sc);
                    break;
                case "4":
                    manageServicesMenu(sc);
                    break;
                case "5":
                    AdminMenu.manageStudentsMenu(sc, userDao);
                    break;
                case "6":
                    back = true;
                    System.out.println("Logging out from Staff menu...");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void viewWaitingQueue() {
        System.out.println("\n==================================================================================================");
        System.out.println("                                    CURRENT WAITING QUEUE                                         ");
        System.out.println("==================================================================================================");

        String sql = "SELECT q.queue_id, q.queue_number, q.student_id, q.service_id, s.student_name, sv.service_name, q.queue_date, q.queue_time, q.status " +
                "FROM Queue q " +
                "JOIN Student s ON q.student_id = s.student_id " +
                "JOIN Services sv ON q.service_id = sv.service_id " +
                "WHERE q.status = 'waiting' " +
                "ORDER BY q.queue_id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("Q.ID\tQ.#\tSTUDENT ID\tSTUDENT NAME\t\tSERVICE\t\tDATE\t\tTIME");
            System.out.println("--------------------------------------------------------------------------------------------------");
            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                System.out.println(
                        rs.getInt("queue_id") + "\t" +
                                rs.getString("queue_number") + "\t" +
                                rs.getInt("student_id") + "\t\t" +
                                rs.getString("student_name") + "\t\t" +
                                rs.getString("service_name") + "\t\t" +
                                rs.getString("queue_date") + "\t" +
                                rs.getString("queue_time")
                );
            }
            if (!hasRows) System.out.println(" No students currently waiting in the queue.");
            System.out.println("==================================================================================================");
        } catch (SQLException e) {
            System.err.println("Error fetching waiting queue: " + e.getMessage());
        }
    }

    private static void processNextQueue(Scanner sc, int staffId) {
        viewWaitingQueue();
        System.out.print("Enter Queue ID to mark as SERVING (or enter 0 to cancel): ");
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

        String updateQueueSql = "UPDATE Queue SET status = 'serving' WHERE queue_id = ? AND status = 'waiting'";
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
                    logStmt.setString(3, "SERVING");
                    logStmt.setString(4, currentDate);
                    logStmt.setString(5, currentTime);
                    logStmt.executeUpdate();

                    conn.commit();
                    System.out.println("-> Success! Queue ID " + queueId + " marked as SERVING and logged.");
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

    // --- COMPLETE QUEUE MANAGEMENT (CRUD) ---
    private static void manageQueueMenu(Scanner sc) {
        boolean back = false;
        while (!back) {
            System.out.println("\n=======================================");
            System.out.println("          QUEUE MANAGEMENT (CRUD)      ");
            System.out.println("=======================================");
            System.out.println("1. Add Queue Entry (Create)");
            System.out.println("2. View All Queue Records (Read)");
            System.out.println("3. Update Queue Entry Status (Update)");
            System.out.println("4. Delete Queue Entry (Delete)");
            System.out.println("5. Back to Dashboard");
            System.out.print("Choose an option: ");

            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    AdminMenu.viewStudentDirectory();
                    System.out.print("Enter Student ID: ");
                    int studentId = Integer.parseInt(sc.nextLine());

                    viewServicesDirectory();
                    System.out.print("Enter Service ID: ");
                    int serviceId = Integer.parseInt(sc.nextLine());

                    if (!isStudentExists(studentId) || !isServiceExists(serviceId)) {
                        System.out.println("-> Error: Invalid Student ID or Service ID!");
                        break;
                    }

                    System.out.print("Enter Queue Number (e.g., A-001): ");
                    String queueNumber = sc.nextLine().trim();

                    String currentDate = LocalDate.now().toString();
                    String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

                    String insertSql = "INSERT INTO Queue (queue_number, student_id, service_id, queue_date, queue_time, status) VALUES (?, ?, ?, ?, ?, 'waiting')";
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                        pstmt.setString(1, queueNumber);
                        pstmt.setInt(2, studentId);
                        pstmt.setInt(3, serviceId);
                        pstmt.setString(4, currentDate);
                        pstmt.setString(5, currentTime);
                        pstmt.executeUpdate();
                        System.out.println("-> Success! Queue entry added.");
                    } catch (SQLException e) {
                        System.err.println("Error adding queue entry: " + e.getMessage());
                    }
                    break;

                case "2":
                    viewAllQueues();
                    break;

                case "3":
                    viewAllQueues();
                    System.out.print("Enter Queue ID to Update: ");
                    int updateId = Integer.parseInt(sc.nextLine());

                    System.out.print("Enter New Queue Number: ");
                    String newQNum = sc.nextLine().trim();

                    String newStatus = readValidServiceStatus(sc);

                    String updateSql = "UPDATE Queue SET queue_number = ?, status = ? WHERE queue_id = ?";
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                        pstmt.setString(1, newQNum);
                        pstmt.setString(2, newStatus);
                        pstmt.setInt(3, updateId);
                        int rows = pstmt.executeUpdate();
                        if (rows > 0) {
                            System.out.println("-> Success! Queue ID " + updateId + " updated.");
                        } else {
                            System.out.println("-> Update failed. Queue ID not found.");
                        }
                    } catch (SQLException e) {
                        System.err.println("Error updating queue: " + e.getMessage());
                    }
                    break;

                case "4":
                    viewAllQueues();
                    System.out.print("Enter Queue ID to Delete (or 0 to cancel): ");
                    int deleteId = Integer.parseInt(sc.nextLine());
                    if (deleteId != 0) {
                        String deleteSql = "DELETE FROM Queue WHERE queue_id = ?";
                        try (Connection conn = DatabaseConnection.getConnection();
                             PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                            pstmt.setInt(1, deleteId);
                            int rows = pstmt.executeUpdate();
                            if (rows > 0) {
                                System.out.println("-> Success! Queue ID " + deleteId + " deleted.");
                            } else {
                                System.out.println("-> Deletion failed. Queue ID not found.");
                            }
                        } catch (SQLException e) {
                            System.err.println("Error deleting queue: " + e.getMessage());
                        }
                    }
                    break;

                case "5":
                    back = true;
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void viewAllQueues() {
        System.out.println("\n==================================================================================");
        System.out.println("                              ALL QUEUE RECORDS                                   ");
        System.out.println("==================================================================================");
        String sql = "SELECT queue_id, queue_number, student_id, service_id, queue_date, queue_time, status FROM Queue";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("ID\tQ.#\tSTUDENT ID\tSERVICE ID\tDATE\t\tTIME\t\tSTATUS");
            System.out.println("--------------------------------------------------------------------------------==");
            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                System.out.println(
                        rs.getInt("queue_id") + "\t" +
                                rs.getString("queue_number") + "\t" +
                                rs.getInt("student_id") + "\t\t" +
                                rs.getInt("service_id") + "\t\t" +
                                rs.getString("queue_date") + "\t" +
                                rs.getString("queue_time") + "\t" +
                                rs.getString("status")
                );
            }
            if (!hasRows) System.out.println(" No queue records found.");
            System.out.println("==================================================================================");
        } catch (SQLException e) {
            System.err.println("Error fetching queues: " + e.getMessage());
        }
    }

    private static void manageServicesMenu(Scanner sc) {
        boolean back = false;
        while (!back) {
            System.out.println("\n=======================================");
            System.out.println("         SERVICES MANAGEMENT (CRUD)    ");
            System.out.println("=======================================");
            System.out.println("1. Add New Service");
            System.out.println("2. View All Services");
            System.out.println("3. Update Service Record");
            System.out.println("4. Delete Service Record");
            System.out.println("5. Back to Staff Dashboard");
            System.out.print("Choose an option: ");

            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Enter Service Name: ");
                    String name = sc.nextLine();
                    System.out.print("Enter Description: ");
                    String desc = sc.nextLine();
                    System.out.print("Enter Counter Number: ");
                    int counter = Integer.parseInt(sc.nextLine());

                    String status = readValidServiceStatus(sc);

                    String insertSql = "INSERT INTO Services (service_name, description, counter_number, status) VALUES (?, ?, ?, ?)";
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                        pstmt.setString(1, name);
                        pstmt.setString(2, desc);
                        pstmt.setInt(3, counter);
                        pstmt.setString(4, status);
                        pstmt.executeUpdate();
                        System.out.println("-> Success! New service added.");
                    } catch (SQLException e) {
                        System.err.println("Error adding service: " + e.getMessage());
                    }
                    break;
                case "2":
                    viewServicesDirectory();
                    break;
                case "3":
                    viewServicesDirectory();
                    System.out.print("Enter Service ID to Update: ");
                    int updateId = Integer.parseInt(sc.nextLine());
                    System.out.print("Enter New Service Name: ");
                    String newName = sc.nextLine();
                    System.out.print("Enter New Description: ");
                    String newDesc = sc.nextLine();
                    System.out.print("Enter New Counter Number: ");
                    int newCounter = Integer.parseInt(sc.nextLine());

                    String newServiceStatus = readValidServiceStatus(sc);

                    String updateSql = "UPDATE Services SET service_name = ?, description = ?, counter_number = ?, status = ? WHERE service_id = ?";
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                        pstmt.setString(1, newName);
                        pstmt.setString(2, newDesc);
                        pstmt.setInt(3, newCounter);
                        pstmt.setString(4, newServiceStatus);
                        pstmt.setInt(5, updateId);
                        int rows = pstmt.executeUpdate();
                        if (rows > 0) {
                            System.out.println("-> Success! Service ID " + updateId + " updated.");
                        } else {
                            System.out.println("-> Update failed. Service ID not found.");
                        }
                    } catch (SQLException e) {
                        System.err.println("Error updating service: " + e.getMessage());
                    }
                    break;
                case "4":
                    viewServicesDirectory();
                    System.out.print("Enter Service ID to Delete (or 0 to cancel): ");
                    int deleteId = Integer.parseInt(sc.nextLine());
                    if (deleteId != 0) {
                        String deleteSql = "DELETE FROM Services WHERE service_id = ?";
                        try (Connection conn = DatabaseConnection.getConnection();
                             PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                            pstmt.setInt(1, deleteId);
                            int rows = pstmt.executeUpdate();
                            if (rows > 0) {
                                System.out.println("-> Success! Service ID " + deleteId + " deleted.");
                            } else {
                                System.out.println("-> Deletion failed. Service ID not found.");
                            }
                        } catch (SQLException e) {
                            System.err.println("Error deleting service: " + e.getMessage());
                        }
                    }
                    break;
                case "5":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static String readValidServiceStatus(Scanner sc) {
        while (true) {
            System.out.print("Enter Status [waiting, serving, completed, cancelled]: ");
            String status = sc.nextLine().trim().toLowerCase();
            if (status.equals("waiting") || status.equals("serving") || status.equals("completed") || status.equals("cancelled")) {
                return status;
            }
            System.out.println("Invalid status! Please enter exactly: waiting, serving, completed, or cancelled.");
        }
    }

    private static void viewServicesDirectory() {
        System.out.println("\n==================================================================================");
        System.out.println("                               SERVICES DIRECTORY                                 ");
        System.out.println("==================================================================================");
        String sql = "SELECT service_id, service_name, description, counter_number, status FROM Services";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("ID\tNAME\t\tDESCRIPTION\t\tCOUNTER\tSTATUS");
            System.out.println("--------------------------------------------------------------------------------==");
            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                System.out.println(
                        rs.getInt("service_id") + "\t" +
                                rs.getString("service_name") + "\t\t" +
                                rs.getString("description") + "\t\t" +
                                rs.getInt("counter_number") + "\t" +
                                rs.getString("status")
                );
            }
            if (!hasRows) System.out.println(" No service records found.");
            System.out.println("==================================================================================");
        } catch (SQLException e) {
            System.err.println("Error fetching services: " + e.getMessage());
        }
    }

    private static int getStaffIdByUsername(String username) {
        String sql = "SELECT staff_id FROM Staff WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("staff_id");
            }
        } catch (SQLException e) {
            // Fallback
        }
        return 1;
    }

    private static boolean isStudentExists(int studentId) {
        String sql = "SELECT 1 FROM Student WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private static boolean isServiceExists(int serviceId) {
        String sql = "SELECT 1 FROM Services WHERE service_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, serviceId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }
}