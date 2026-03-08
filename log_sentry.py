import sys
import argparse
import logging
import re
import json
import sqlite3
import os

logging.basicConfig(
    level=logging.INFO,
    format='%(levelname)s: %(message)s'
)

LOG_PATTERN = re.compile(
    r"^(?P<timestamp>[A-Z][a-z]{2}\s+\d{1,2}\s+\d{2}:\d{2}:\d{2})\s+"
    r"(?P<hostname>\S+)\s+"
    r"(?P<service>[^:]+):\s+"
    r"(?P<message>.*)$"                                                                                            # Captures the rest
)


DB_FILE = os.environ.get("LOG_SENTRY_DB", "logs.db")

def setup_database():
    connection = sqlite3.connect(DB_FILE)
    cursor = connection.cursor()

    cursor.execute('''
        CREATE TABLE IF NOT EXISTS logs (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            timestamp TEXT,
            hostname TEXT,
            service TEXT,
            level TEXT,
            message TEXT
        )
    ''')

    cursor.execute('''
        CREATE INDEX IF NOT EXISTS idx_level ON logs (level)
    ''')

    connection.commit()
    connection.close()
    logging.info(f"Database setup complete.")

def parse_line(line):
    match = LOG_PATTERN.match(line)

    if match:
        data = match.groupdict()
        msg_lower = data['message'].lower()
        if 'error' in msg_lower or 'fail' in msg_lower or 'fatal' in msg_lower:
            data['level'] = 'ERROR'
        elif 'warn' in msg_lower:
            data['level'] = 'WARNING'
        else:
            data['level'] = 'INFO'
        return data
    else:
        return None


def ingest_logs():

    setup_database()
    connection = sqlite3.connect(DB_FILE)
    cursor = connection.cursor()

    logging.info(f"Starting log ingestion. Press Ctrl+C to stop.")

    try:
        for line in sys.stdin:
            clean_line = line.strip()
            structured_log = parse_line(clean_line)

            if structured_log:
                cursor.execute('''
                    INSERT INTO logs (timestamp,hostname, service, level, message)
                    VALUES (?, ?, ?, ?, ?)
                ''', (
                    structured_log['timestamp'],
                    structured_log['hostname'],
                    structured_log['service'],
                    structured_log['level'],
                    structured_log['message']
                ))
                connection.commit()
                logging.debug(f"Saved to DB: [{structured_log['level']}] {structured_log['message']}")
            else:
                logging.warning(f"Could not parse line: {clean_line}")

    except KeyboardInterrupt:
        print()
        logging.info(f"Ingestion stopped by user. Shitting down gracefully.")
    finally:
        connection.close()
        logging.info(f"Database connection closed safely.")

def query_logs(level_filter, limit):
    setup_database()
    logging.debug(f"Querying DB: filter={level_filter}, limit={limit}")
    connection = sqlite3.connect(DB_FILE)
    cursor = connection.cursor()

    print(f"\n--- Showing Last {limit} Logs ---")

    if level_filter:
        cursor.execute('''
            SELECT timestamp, level, message
            FROM logs
            WHERE level = ?
            ORDER BY id DESC
            LIMIT ?
        ''', (level_filter.upper(), limit))
    else:
        cursor.execute('''
            SELECT timestamp, level, message
            FROM logs
            ORDER BY id DESC
            LIMIT ?
        ''', (limit,))

    rows = cursor.fetchall()

    for row in rows:
        print(f"[{row[0]}] {row[1]}: {row[2]}")

    print("\n--- Total Log Summary ---")
    cursor.execute('SELECT level, COUNT(*) FROM logs GROUP BY level')
    summary_rows = cursor.fetchall()

    for row in summary_rows:
        print(f"{row[0]}: {row[1]} total logs")

    connection.close()

def detect_anomalies(window_size, error_threshold):
    setup_database()
    logging.debug(f"Running anomaly detection: window={window_size}, threshold={error_threshold}")
    connection = sqlite3.connect(DB_FILE)
    cursor = connection.cursor()

    cursor.execute('''
        SELECT level FROM logs
        ORDER BY id DESC
        LIMIT ?
        ''', (window_size,))

    recent_logs = cursor.fetchall()
    connection.close()

    if len(recent_logs) == 0:
        print("No logs found in the database to analyze.")
        return

    error_count = 0
    warning_count = 0

    for row in recent_logs:
        if row[0] == 'ERROR':
            error_count += 1
        elif row[0] == 'WARNING':
            warning_count += 1

    print(f"\n--- Anomaly Detection Report ---")
    print(f"Analyzing the last {len(recent_logs)} logs (Sliding Window)...")
    print(f"Found {error_count} ERRORs and {warning_count} WARNINGs.\n")

    if error_count >= error_threshold:
        print(f"ANOMALY DETECTED!")
        print(f"Error rate is critically high: {error_count} errors.")
        print(f"This exceeds the threshold of {error_threshold}. Investigate immediately.")
    elif warning_count >= (error_threshold * 2):
        print(f"WARNING SPIKE")
        print(f"High number of warnings detected ({warning_count}). System might be degrading")
    else:
        print(f"System Health Normal. No anomalies detected in this window.")

def main():
    parser = argparse.ArgumentParser(description="Log Sentry - System log analyzer")
    parser.add_argument("--verbose", action="store_true", help="Enable developer debug logs")
    subparsers = parser.add_subparsers(dest="command", required=True)
    ingest_parser = subparsers.add_parser("ingest", help="Ingest raw logs from a stream")

    query_parser = subparsers.add_parser("query", help="Query logs from the database")
    query_parser.add_argument("--level", type=str, help="Filter by log level (INFO, WARNING, ERROR)")
    query_parser.add_argument("--limit", type=int, default=10, help="Maximum number of logs to show")

    anomaly_parser = subparsers.add_parser("anomalies", help="Run anomaly detection on recent logs")
    anomaly_parser.add_argument("--window", type=int, default=100, help="Number of recent logs to analyze")
    anomaly_parser.add_argument("--threshold", type=int, default=15, help="Number of errors to trigger an alarm")

    args = parser.parse_args()

    if args.verbose:
        logging.basicConfig(level=logging.DEBUG)
        logging.debug("Verbose mode enabled! Showing behind the scenes data.")
    if args.command == "ingest":
        ingest_logs()
    elif args.command == "query":
        query_logs(args.level, args.limit)

    elif args.command == "anomalies":
        detect_anomalies(args.window, args.threshold)

if __name__ == "__main__":
    main()
