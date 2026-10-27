package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        String userTable = "CREATE TABLE IF NOT EXISTS users ("
                + "user_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT NOT NULL UNIQUE, "
                + "password TEXT NOT NULL, "
                + "role TEXT NOT NULL, "
                + "status TEXT NOT NULL DEFAULT 'PENDING'"
                + ")";

        String studentTable = "CREATE TABLE IF NOT EXISTS Student ("
                + "student_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "student_name TEXT NOT NULL, "
                + "course TEXT, "
                + "year_level INTEGER, "
                + "contact_number TEXT, "
                + "status TEXT NOT NULL DEFAULT 'ACTIVE'"
                + ")";

        String servicesTable = "CREATE TABLE IF NOT EXISTS Services ("
                + "service_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "service_name TEXT NOT NULL, "
                + "description TEXT, "
                + "counter_number INTEGER, "
                + "status TEXT NOT NULL DEFAULT 'ACTIVE'"
                + ")";

        String queueTable = "CREATE TABLE IF NOT EXISTS Queue ("
                + "queue_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "queue_number TEXT NOT NULL, "
                + "student_id INTEGER NOT NULL, "
                + "service_id INTEGER NOT NULL, "
                + "queue_date TEXT, "
                + "queue_time TEXT, "
                + "status TEXT NOT NULL DEFAULT 'WAITING', "
                + "FOREIGN KEY (student_id) REFERENCES Student(student_id), "
                + "FOREIGN KEY (service_id) REFERENCES Services(service_id)"
                + ")";

        String staffTable = "CREATE TABLE IF NOT EXISTS Staff ("
                + "staff_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "staff_name TEXT NOT NULL, "
                + "username TEXT NOT NULL UNIQUE, "
                + "password TEXT NOT NULL, "
                + "role TEXT NOT NULL, "
                + "status TEXT NOT NULL DEFAULT 'ACTIVE'"
                + ")";

        String logTable = "CREATE TABLE IF NOT EXISTS QueueActivityLog ("
                + "log_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "queue_id INTEGER NOT NULL, "
                + "staff_id INTEGER NOT NULL, "
                + "action TEXT NOT NULL, "
                + "date TEXT, "
                + "time TEXT, "
                + "FOREIGN KEY (queue_id) REFERENCES Queue(queue_id), "
                + "FOREIGN KEY (staff_id) REFERENCES Staff(staff_id)"
                + ")";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(userTable);
            statement.execute(studentTable);
            statement.execute(servicesTable);
            statement.execute(queueTable);
            statement.execute(staffTable);
            statement.execute(logTable);

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            System.out.println("Database initialization failed: " + e.getMessage());
        }
    }
}