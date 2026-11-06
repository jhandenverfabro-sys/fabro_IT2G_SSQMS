package com.scc.ui;

import com.scc.Model.User;
import com.scc.dao.UserDao;
import java.util.Scanner;

public class Authentication {
    public static final Scanner sc = new Scanner(System.in);
    private static final UserDao userDao = new UserDao();

    public static void start() {
        boolean running = true;
        String loggedInUsername = null;
        int loggedInUserId = -1;
        String loggedInRole = null;

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
                        System.out.println("\nLogin Successful! Role: " + role);

                        loggedInUsername = username;
                        loggedInUserId = userDao.getUserIdByUsername(username);
                        loggedInRole = role;

                        switch (role) {
                            case "ADMIN":
                                AdminMenu.showMenu(sc, userDao, new User(loggedInUserId, username, "", "ADMIN", "ACTIVE"));
                                break;
                            case "STUDENT":
                                StudentMenu.showMenu(sc, username);
                                break;
                            case "STAFF":
                                StaffMenu.showMenu(sc, username);
                                break;
                            case "SUPERVISOR":
                                SupervisorMenu.showMenu(sc, username);
                                break;
                            default:
                                System.out.println("Welcome, " + role + "!");
                        }

                        // FIXED: Pass 3 arguments (userId, username, role) to match UserDao.logLogout()
                        userDao.logLogout(loggedInUserId, loggedInUsername, loggedInRole);
                        loggedInUsername = null;
                        loggedInUserId = -1;
                        loggedInRole = null;

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
                    System.out.println("[1] ADMIN");
                    System.out.println("[2] STAFF");
                    System.out.println("[3] SUPERVISOR");
                    System.out.print("Choose role option: ");

                    int roleChoice;
                    try {
                        roleChoice = Integer.parseInt(sc.nextLine().trim());
                    } catch (NumberFormatException e) {
                        roleChoice = -1;
                    }

                    String registerRole = switch (roleChoice) {
                        case 1 -> "ADMIN";
                        case 2 -> "STAFF";
                        case 3 -> "SUPERVISOR";
                        default -> null;
                    };

                    if (registerRole != null) {
                        userDao.registerUser(user, pass, registerRole, "PENDING");
                        System.out.println("Registration successful. Account is pending approval.");
                    } else {
                        System.out.println("Invalid role selection!");
                    }
                    break;

                case "3":
                    if (loggedInUsername != null) {
                        // FIXED: Pass 3 arguments here as well
                        userDao.logLogout(loggedInUserId, loggedInUsername, loggedInRole);
                    }
                    System.out.println("Exiting application. Goodbye!");
                    running = false;
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}