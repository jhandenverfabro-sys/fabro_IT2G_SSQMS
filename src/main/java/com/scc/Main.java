package com.scc;

import com.scc.dao.userDao;

import java.util.Scanner;

public class Main {
    public static final Scanner sc = new Scanner(System.in);
    private static final userDao userDao = new userDao();

    public static void main(String[] args) {
        System.out.println("\n=======================================");
        System.out.println(" STUDENT SERVICE QUEUE MANAGEMENT SYSTEM ");
        System.out.println("=========================================");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.println("Choose an option: ");
        String choice = sc.nextLine();

        switch (choice) {
            case "1":
                System.out.println();
                System.out.println("============LOGIN===========");
                System.out.println("Enter username: ");
                String username = sc.nextLine();
                System.out.println("Enter password: ");
                String password = sc.nextLine();

                String role = userDao.loginUser(username, password);

                if (role != null){
                    System.out.println();
                    System.out.println("Login Successful!");
                    System.out.println("role: "+ role);

                    if (role.equals("ADMIN")){
                        System.out.println("Welcome, Admin!");
                    }else if(role.equals("TEACHER")) {
                        System.out.println("Welcome, Teacher!");
                    }
                }else {
                    System.out.println("Invalid user or password!");
                }
                break;
            case "2":
                System.out.println("Enter new username: ");
                String user = sc.nextLine();
                System.out.println("Enter new password: ");
                String pass = sc.nextLine();

                System.out.println("Select Role [1] TEACHER [2] ADMIN: ");
                int roleChoice = sc.nextInt();
                String registerRole;
                switch (roleChoice) {
                    case 1:
                        registerRole = "TEACHER";
                        break;
                    case 2:
                        registerRole = "ADMIN";
                        break;
                    default:
                        registerRole = null;
                }

                if (registerRole != null) {
                    String status ="Inactive";
                    userDao.registerUser(user, pass, registerRole, status);
                } else {
                    System.out.println("Invalid role selection!");
                }
                break;

        }
    }

}
