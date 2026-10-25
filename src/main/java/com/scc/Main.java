package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.ui.Authentication;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Student Service Queue Management System...");

        // 1. Initialize Database and ensure all required tables exist
        DatabaseInitializer.initialize();

        // 2. Delegate execution to the Authentication UI handler
        Authentication.start();
    }
}