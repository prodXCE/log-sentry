package com.sentry;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class Database {

    private static final String DB_URL = "jdbc:sqlite:data/db/sentry.db";

    public static void initialize() {

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("CRITICAL: SQLite Driver not found! Did you include the jar?");
            System.exit(1);
        }

        try (Connection conn = DriverManager.getConnection(DB_URL);
            Statement stmt = conn.createStatement()) {

                String sql = "CREATE TABLE IF NOT EXISTS logs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "timestamp TEXT, " +
                    "host TEXT, " +
                    "component TEXT, " +
                    "message TEXT" +
                    ");";

            stmt.execute(sql);
            System.out.println("[DB] Database initialized at " + DB_URL);


            } catch (SQLException e) {
                System.err.println("[DB] Error initializing: " + e.getMessage());
            }
    }
}
