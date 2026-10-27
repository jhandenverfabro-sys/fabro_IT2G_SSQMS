package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.ui.Authentication;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Student Service Queue Management System...");

        // 1. Initialize tables and foreign keys
        DatabaseInitializer.initialize();

        // 2. Launch authentication console
        Authentication.start();
    }
}