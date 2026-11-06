package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.ui.Authentication;

public class Main {
    public static void main(String[] args) {
        System.out.println("Student Service Queue Management System is running...");

        // 1. Initialize tables and constraints
        DatabaseInitializer.initialize();

        // 2. Launch console interface
        Authentication.start();
    }
}