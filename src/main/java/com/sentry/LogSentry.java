package com.sentry;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogSentry {
    public static void main(String[] args) {
            Database.initialize();

            // CHECK: Did the user ask for the report?
            if (args.length > 0 && args[0].equals("--report")) {
                System.out.println("Generating Baseline Report...");
                Database.printStatistics();
                return; // STOP here. Do not listen for logs.
            }

            if (args.length > 0 && args[0].equals("--check")) {
                int threshold = 5;
                if (args.length > 1) {
                    threshold = Integer.parseInt(args[1]);
                }
                Database.checkAnomalies(threshold);
                return;
            }

            // --- NORMAL MODE (Listening to Stdin) ---
            // (This code stays exactly the same as before)
            String regex = "^([A-Z][a-z]{2}\\s+\\d+\\s\\d{2}:\\d{2}:\\d{2})\\s+(\\S+)\\s+([^:]+):\\s+(.*)$";
            Pattern pattern = Pattern.compile(regex);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        LogEntry entry = new LogEntry(
                            matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4)
                        );
                        Database.save(entry);
                        // Minimal output now, just a dot to show activity
                        System.out.print(".");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}
