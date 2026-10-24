package com.scc.ui;

import com.scc.dao.userDao;
import java.util.Scanner;

public class Authentication {
    public static final Scanner sc = new Scanner(System.in);
    private static final userDao userDao = new userDao();

    public static void start() {
        boolean running = true;

        while (running) {
            System.out.println("\n=======================================");
            System.out.println(" STUDENT SERVICE QUEUE MANAGEMENT SYSTEM ");
            System.out.println("=========================================");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            if (!sc.hasNextLine()) break;
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    System.out.println("\n============ LOGIN ============");
                    System.out.print("Enter username: ");
                    String username = sc.nextLine();
                    System.out.print("Enter password: ");
                    String password = sc.nextLine();

                    String role = userDao.loginUser(username, password);

                    if (role != null) {
                        System.out.println("\nLogin Successful!");
                        System.out.println("Role: " + role);

                        switch (role) {
                            case "ADMIN":
                                System.out.println("Welcome, Admin!");
                                break;
                            case "STUDENT":
                                System.out.println("Welcome, Student!");
                                break;
                            case "STAFF":
                                System.out.println("Welcome, Staff!");
                                break;
                            case "SUPERVISOR":
                                System.out.println("Welcome, Supervisor!");
                                break;
                            default:
                                System.out.println("Welcome!");
                        }
                    } else {
                        System.out.println("Invalid username, password, or account pending approval!");
                    }
                    break;

                case "2":
                    System.out.println("\n========== REGISTRATION ==========");
                    System.out.print("Enter new username: ");
                    String user = sc.nextLine();
                    System.out.print("Enter new password: ");
                    String pass = sc.nextLine();

                    System.out.println("Select Role:");
                    System.out.println("[1] STUDENT");
                    System.out.println("[2] ADMIN");
                    System.out.println("[3] STAFF");
                    System.out.println("[4] SUPERVISOR");
                    System.out.print("Choose role option: ");

                    int roleChoice;
                    try {
                        roleChoice = Integer.parseInt(sc.nextLine());
                    } catch (NumberFormatException e) {
                        roleChoice = -1;
                    }

                    String registerRole;
                    switch (roleChoice) {
                        case 1:
                            registerRole = "STUDENT";
                            break;
                        case 2:
                            registerRole = "ADMIN";
                            break;
                        case 3:
                            registerRole = "STAFF";
                            break;
                        case 4:
                            registerRole = "SUPERVISOR";
                            break;
                        default:
                            registerRole = null;
                    }

                    if (registerRole != null) {
                        String status = "PENDING";
                        userDao.registerUser(user, pass, registerRole, status);
                    } else {
                        System.out.println("Invalid role selection!");
                    }
                    break;

                case "3":
                    System.out.println("Exiting application. Goodbye!");
                    running = false;
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}