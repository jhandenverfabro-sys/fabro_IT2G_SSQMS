package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        // 1. Users Table (Matches your DB structure: user_id, username, password, role, status)
        String usersTable = "CREATE TABLE IF NOT EXISTS users ("
                + "user_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT NOT NULL UNIQUE, "
                + "password TEXT NOT NULL, "
                + "role TEXT NOT NULL, "
                + "status TEXT NOT NULL DEFAULT 'PENDING'"
                + ")";

        // 2. Student Table (Matches your DB structure: student_id, student_name, course, year_level, contact_number, status)
        String studentTable = "CREATE TABLE IF NOT EXISTS Student ("
                + "student_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "student_name TEXT NOT NULL, "
                + "course TEXT, "
                + "year_level INTEGER, "
                + "contact_number INTEGER, "
                + "status TEXT NOT NULL DEFAULT 'ACTIVE'"
                + ")";

        // 3. Services Table (Matches your DB structure: service_id, service_name, description, counter_number, status)
        String servicesTable = "CREATE TABLE IF NOT EXISTS Services ("
                + "service_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "service_name TEXT NOT NULL, "
                + "description TEXT, "
                + "counter_number INTEGER, "
                + "status TEXT NOT NULL DEFAULT 'ACTIVE'"
                + ")";

        // 4. Queue Table (Matches your DB structure: queue_id, queue_number, student_id, service_id, queue_date, queue_time, status)
        String queueTable = "CREATE TABLE IF NOT EXISTS Queue ("
                + "queue_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "queue_number TEXT NOT NULL, "
                + "student_id INTEGER NOT NULL, "
                + "service_id INTEGER NOT NULL, "
                + "queue_date TEXT, "
                + "queue_time TEXT, "
                + "status TEXT NOT NULL DEFAULT 'WAITING', "
                + "FOREIGN KEY (student_id) REFERENCES Student(student_id) ON DELETE CASCADE, "
                + "FOREIGN KEY (service_id) REFERENCES Services(service_id) ON DELETE CASCADE"
                + ")";

        // 5. Staff Table (Matches your DB structure)
        String staffTable = "CREATE TABLE IF NOT EXISTS Staff ("
                + "staff_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "staff_name TEXT NOT NULL, "
                + "username TEXT NOT NULL UNIQUE, "
                + "password TEXT NOT NULL, "
                + "role TEXT NOT NULL, "
                + "status TEXT NOT NULL DEFAULT 'ACTIVE'"
                + ")";

        // 6. QueueActivityLog Table (Matches your DB structure: log_id, queue_id, staff_id, action, date, time)
        String queueActivityLogTable = "CREATE TABLE IF NOT EXISTS QueueActivityLog ("
                + "log_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "queue_id INTEGER NOT NULL, "
                + "staff_id INTEGER NOT NULL, "
                + "action TEXT NOT NULL, "
                + "date TEXT, "
                + "time TEXT, "
                + "FOREIGN KEY (queue_id) REFERENCES Queue(queue_id) ON DELETE CASCADE, "
                + "FOREIGN KEY (staff_id) REFERENCES Staff(staff_id) ON DELETE CASCADE"
                + ")";

        // 7. Activity Logs Table (Matches your DB structure: id, user_id, action_type, description, timestamp)
        String activityLogTable = "CREATE TABLE IF NOT EXISTS activity_logs ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id INTEGER NOT NULL, "
                + "action_type TEXT NOT NULL, "
                + "description TEXT, "
                + "timestamp TEXT DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE"
                + ")";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(usersTable);
            statement.execute(studentTable);
            statement.execute(servicesTable);
            statement.execute(queueTable);
            statement.execute(staffTable);
            statement.execute(queueActivityLogTable);
            statement.execute(activityLogTable);

            System.out.println("All SSQMS database tables bound and initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Database table binding failed: " + e.getMessage());
        }
    }
}