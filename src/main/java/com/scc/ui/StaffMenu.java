package com.scc.ui;

import com.scc.dao.DatabaseConnection;
import com.scc.dao.QueueDao;
import com.scc.dao.ServiceDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class StaffMenu {
    private static final QueueDao queueDao = new QueueDao();
    private static final ServiceDao serviceDao = new ServiceDao();

    public static void showMenu(Scanner sc, String staffUsername) {
        boolean back = false;

        while (!back) {
            System.out.println("\n=======================================");
            System.out.println("             STAFF DASHBOARD           ");
            System.out.println("=========================================");
            System.out.println("1. View Waiting Queue");
            System.out.println("2. Process Next Queue");
            System.out.println("3. Manage Services");
            System.out.println("4. Manage Verified Students");
            System.out.println("5. Logout");
            System.out.print("Choose an option: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    viewWaitingQueue();
                    break;
                case "2":
                    processNextQueue(sc);
                    break;
                case "3":
                    manageServicesMenu(sc);
                    break;
                case "4":
                    AdminMenu.manageStudentsMenu(sc);
                    break;
                case "5":
                    back = true;
                    System.out.println("Logging out from Staff menu...");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void viewWaitingQueue() {
        System.out.println("\n==================================================================================================");
        System.out.println("                                    CURRENT WAITING QUEUE                                         ");
        System.out.println("==================================================================================================");

        String sql = "SELECT q.queue_id, q.queue_number, s.student_name, sv.service_name, q.queue_date, q.queue_time " +
                "FROM Queue q JOIN Student s ON q.student_id = s.student_id JOIN Services sv ON q.service_id = sv.service_id " +
                "WHERE q.status = 'WAITING' ORDER BY q.queue_id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("ID\tQ.#\tSTUDENT NAME\t\tSERVICE\t\tDATE\t\tTIME");
            System.out.println("--------------------------------------------------------------------------------------------------");
            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                System.out.println(
                        rs.getInt("queue_id") + "\t" +
                                rs.getString("queue_number") + "\t" +
                                rs.getString("student_name") + "\t\t" +
                                rs.getString("service_name") + "\t\t" +
                                rs.getString("queue_date") + "\t" +
                                rs.getString("queue_time")
                );
            }
            if (!hasRows) System.out.println(" No students currently waiting.");
            System.out.println("==================================================================================================");
        } catch (SQLException e) {
            System.err.println("Error fetching waiting queue: " + e.getMessage());
        }
    }

    private static void processNextQueue(Scanner sc) {
        viewWaitingQueue();
        int queueId = Authentication.readIntInput(sc, "Enter Queue ID to mark as SERVING (0 to cancel): ");
        if (queueId == 0) return;

        if (queueDao.updateQueueStatus(queueId, "SERVING")) {
            System.out.println("-> Success! Queue ID " + queueId + " is now SERVING.");
        } else {
            System.out.println("-> Failed to update queue status.");
        }
    }

    private static void manageServicesMenu(Scanner sc) {
        boolean back = false;
        while (!back) {
            System.out.println("\n=======================================");
            System.out.println("         SERVICES MANAGEMENT           ");
            System.out.println("=======================================");
            System.out.println("1. Add New Service");
            System.out.println("2. View All Services");
            System.out.println("3. Update Service");
            System.out.println("4. Delete Service");
            System.out.println("5. Back");
            System.out.print("Choose an option: ");

            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1":
                    System.out.print("Enter Service Name: ");
                    String name = sc.nextLine();
                    System.out.print("Enter Description: ");
                    String desc = sc.nextLine();
                    int counter = Authentication.readIntInput(sc, "Enter Counter Number: ");
                    if (serviceDao.addService(name, desc, counter, "ACTIVE")) {
                        System.out.println("-> Service added successfully.");
                    }
                    break;
                case "2":
                    viewServicesDirectory();
                    break;
                case "3":
                    viewServicesDirectory();
                    int updateId = Authentication.readIntInput(sc, "Enter Service ID to Update: ");
                    System.out.print("Enter New Name: ");
                    String newName = sc.nextLine();
                    System.out.print("Enter New Description: ");
                    String newDesc = sc.nextLine();
                    int newCounter = Authentication.readIntInput(sc, "Enter New Counter: ");
                    if (serviceDao.updateService(updateId, newName, newDesc, newCounter, "ACTIVE")) {
                        System.out.println("-> Service updated successfully.");
                    }
                    break;
                case "4":
                    viewServicesDirectory();
                    int deleteId = Authentication.readIntInput(sc, "Enter Service ID to Delete (0 to cancel): ");
                    if (deleteId != 0 && serviceDao.deleteService(deleteId)) {
                        System.out.println("-> Service deleted successfully.");
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
            while (rs.next()) {
                System.out.println(
                        rs.getInt("service_id") + "\t" +
                                rs.getString("service_name") + "\t\t" +
                                rs.getString("description") + "\t\t" +
                                rs.getInt("counter_number") + "\t" +
                                rs.getString("status")
                );
            }
            System.out.println("==================================================================================");
        } catch (SQLException e) {
            System.err.println("Error fetching services: " + e.getMessage());
        }
    }
}