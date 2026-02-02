package com.sentry;

public class LogEntry {

    public final String fullDate;
    public final String host;
    public final String component;
    public final String message;

    public LogEntry(String fullDate, String host, String component, String message) {
        this.fullDate = fullDate;
        this.host = host;
        this.component = component;
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("[%s] [%s] %s", fullDate, component, message);
    }
}
