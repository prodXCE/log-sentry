# Log Sentry (Linux Log Anomaly Detector)

**Log Sentry** is a lightweight, CLI-first systems tool built to analyze Linux system logs (`journalctl`, `dmesg`).

It follows the **Unix Philosophy**: it does one thing well (analyzing log streams), accepts standard input, and uses a flat-file database (SQLite) for state persistence without requiring a heavy server backend.

**Educational Project**: This tool was built to demonstrate core Systems Programming concepts in Java: Streams, Regex, JDBC, and Process Integration.

## Features

- **Pipeline Architecture**: Reads directly from stdin. Designed to work with pipes (`|`).
- **Structured Parsing**: Converts raw text logs into structured Java Objects using Regex.
- **Persistence**: Stores historical data in an embedded SQLite database.
- **Statistical Baselines**: Uses SQL aggregation (`GROUP BY`) to determine "normal" system behavior.
- **Anomaly Detection**: Flags components that exceed frequency thresholds using `HAVING` clauses.
- **Native Feel**: Includes a shell wrapper to run as a standard system command.

## Project Structure

A clean, filesystem-hierarchy-standard compliant layout:

```
log-sentry/
├── data/
│   ├── db/        # SQLite database file (sentry.db)
│   └── raw/       # (Optional) Raw log exports
├── lib/           # External dependencies (sqlite-jdbc.jar)
├── scripts/       # Shell wrappers for native execution
└── src/
    └── main/java/ # Source code
```

## Prerequisites

- Linux Environment (Ubuntu/Debian tested)
- Java JDK 21+ (Requires `--enable-native-access` for modern FFM/JNI safety)
- SQLite3 (for manual DB inspection)

## Installation

### 1. Setup & Dependencies

Create the structure and download the SQLite JDBC driver.

```bash
mkdir -p lib
# Download the SQLite Driver
curl -o lib/sqlite-jdbc.jar https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.42.0.0/sqlite-jdbc-3.42.0.0.jar
```

### 2. Compile

Compile the source code.

```bash
javac -cp ".:lib/sqlite-jdbc.jar" -d . src/main/java/com/sentry/*.java
```

### 3. Setup the Wrapper (Optional)

Make the tool executable from anywhere.

```bash
chmod +x scripts/sentry
# Link to system bin (Requires sudo)
sudo ln -s $(pwd)/scripts/sentry /usr/local/bin/sentry
```

## Usage

### 1. Ingest Data (Learning Mode)

Feed logs into the system. It will parse and save them to the database.

```bash
# Feed the last 100 lines of the system journal
journalctl -n 100 | sentry
```

### 2. Generate Baseline Report

View the "Top Talkers" in your system to understand normal noise levels.

```bash
sentry --report
```

**Output:**

```
=== TOP 10 LOG SOURCES ===
COMPONENT                      FREQUENCY
------------------------------------------
systemd[1]                     45
CRON[123]                      12
kernel                         8
```

### 3. Detect Anomalies

Check for components that are logging more frequently than a specific threshold.

```bash
# Alert if any component has more than 20 logs
sentry --check 20
```

**Output:**

```
=== ANOMALY DETECTOR (Threshold: 20) ===
[ALERT] High Volume: systemd[1] is noisy! (45 events)
```

## 🔧 Technical Details

- **Parser**: Uses `java.util.regex` with capture groups to extract Timestamp, Host, Component, and Message.
- **Database**: Uses `PreparedStatement` for security (SQL Injection prevention) and performance.
- **Native Access**: The wrapper script handles the `--enable-native-access=ALL-UNNAMED` flag required by modern Java versions to allow the SQLite C-library to run.
