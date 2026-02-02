package com.sentry;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogSentry {
    public static void main(String[] args) {

        String regex = "^([A-Z][a-z]{2}\\s+\\d+\\s\\d{2}:\\d{2}:\\d{2})\\s+(\\S+)\\s+([^:]+):\\s+(.*)$";
        Pattern pattern = Pattern.compile[regex];

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {

            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);

                if (matcher.find()) {
                    String date = matcher.group(1);
                    String host = matcher.group(2);
                    String component = matcher.group(3);
                    String msg = matcher.group(4);

                    LogEntry entry = new LogEntry(date, host, component, msg);

                    System.out.println(entry.toString());

                } else {
                   System.err.println("Failed to parse: " + line);
                }
            }

        } catch (IOException e) {
            System.err.println("Error processing stream: " + e.getMessage());
        }

    }
}
