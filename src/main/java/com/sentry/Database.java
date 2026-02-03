package com.sentry;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:data/db/sentry.db";

    private static Connection conn;

    public static void initialize() {
        try {
            Class.forName("org.sqlite.JDBC");

            conn = DriverManager.getConnection(DB_URL);

            try (Statement stmt = conn.createStatement()) {
                String sql = "CREATE TABLE IF NOT EXISTS logs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "timestamp TEXT, " +
                    "host TEXT, " +
                    "component TEXT, " +
                    "message TEXT" +
                    ");";
                stmt.execute(sql);
            }

            System.out.println("[DB] Connected and Ready.");

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void save(LogEntry entry) {
        String sql = "INSERT INTO logs(timestamp, host, component, message) VALUES(?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, entry.fullDate);
            pstmt.setString(2, entry.host);
            pstmt.setString(3, entry.component);
            pstmt.setString(4, entry.message);

            pstmt.executeUpdate();
        } catch (SQLException e) {

            System.err.println("[DB Error] Could not save log: " + e.getMessage());

        }
    }

    public static void printStatistics() {
        String sql = "SELECT component, COUNT(*) as frequency " +
            "FROM logs " +
            "GROUP BY component " +
            "ORDER BY frequency DESC " +
            "LIMIT 10";

        try (Statement stmt = conn.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery(sql)) {

                System.out.println("=== TOP 10 LOG SOURCES ===");
                System.out.println(String.format("%-30s %s", "COMPONENT", "FREQUENCY"));
                System.out.println("------------------------------------------------");

                while (rs.next()) {
                    String comp = rs.getString("component");
                    int count = rs.getInt("frequency");

                    System.out.printf("%-30s %d%n", comp, count);
                }
            } catch (SQLException e) {
                System.err.println("Error generating report: " + e.getMessage());
            }
    }

    public static void checkAnomalies(int threshold) {
        String sql = "SELECT component, COUNT(*) as frequency " +
            "FROM logs " +
            "GROUP BY component " +
            "HAVING frequency > " + threshold;

        try (Statement stmt = conn.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery(sql)) {
                System.out.println("=== ANOMALY DETECTOR Threshold: " + threshold + ") ===");

                boolean foundAnomaly = false;

                while(rs.next()) {
                    foundAnomaly = true;
                    String comp = rs.getString("component");
                    int count = rs.getInt("frequency");

                    System.err.printf("[ALERT] High Volume: %s is noisy! (%d events)%n", comp, count);
                }

                if (!foundAnomaly) {
                    System.out.println("No anomalies found. System is quiet.");
                }

            } catch (SQLException e) {
                System.err.println("Error checking anomalies: " + e.getMessage());
            }
    }
}
