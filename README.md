# Log Sentry 

> A lightweight, zero-dependency CLI log ingestion, querying, and anomaly detection engine.

Log Sentry is a high-throughput stream processing tool designed for systems engineers. It embraces the classic Unix Philosophy: it reads raw unstructured logs from `stdin`, normalizes them using Regex and heuristics, stores them securely in a local SQLite database, and provides statistical anomaly detection — all without requiring a heavy background agent or cloud connection.

---

## ✨ Key Features

- **Unix Pipe Integration:** Plugs directly into `journalctl`, `tail -f`, or any stream emitting to `stdout`.
- **Zero Dependencies:** Built entirely using the Python Standard Library. No `pip install` required.
- **Smart Parsing & Heuristics:** Uses Regex to extract standard log formats, with intelligent text-analysis fallbacks to detect log severity when explicit tags are missing.
- **SQLite Storage Engine:** Automatically provisions a lightweight, indexed relational database for blazing-fast local querying.
- **Proactive Anomaly Detection:** Utilizes sliding-window statistical analysis to detect dangerous spikes in error rates or system degradation.
- **Production-Ready UX:** Built-in `argparse` routing, graceful keyboard interrupt handling, and verbose developer observability.

---

## 🏗️ Architecture

Log Sentry acts as the receiving end of a Unix pipeline. It is designed to be completely agnostic to the source of the logs.

```
[ Raw Log Stream ]       [ OS Pipe ]         [ Log Sentry ]                 [ SQLite ]
 journalctl -f   ---\                       /--> Regex Normalization ---\
 tail -f /logs   ----|=======( | )========>|                             |---> logs.db
 log_generator   ---/        stdin          \--> Heuristic Analysis  ---/
```

---

## 🚀 Quick Start

Because Log Sentry has zero external dependencies, installation is instant.

```bash
# 1. Clone the repository
git clone https://github.com/prodXCE/log-sentry.git
cd log-sentry

# 2. View the help menu
python log_sentry.py --help
```

---

## 📖 Usage Guide

Log Sentry is divided into three primary command groups: `ingest`, `query`, and `anomalies`.

### 1. Ingestion (Stream Processing)

Pipe any continuous stream of text into Log Sentry. It will silently parse, structure, and save the logs into the database.

**Live System Logs:**

```bash
journalctl -f | python log_sentry.py ingest
```

**Custom Log Generator (Testing):**

```bash
python log_generator.py | python log_sentry.py ingest
```

---

### 2. Querying (Log Analysis)

Stop grepping through massive text files. Ask your database structured questions.

**View the latest 10 logs and a total summary:**

```bash
python log_sentry.py query
```

**Filter for specific severities and limits:**

```bash
python log_sentry.py query --level ERROR --limit 5
python log_sentry.py query --level WARNING
```

---

### 3. Anomaly Detection

Run proactive health checks against your recent system data. Log Sentry uses a sliding window to analyze recent log frequencies against safety thresholds.

**Run a standard health check:**

```bash
python log_sentry.py anomalies
```

**Custom Windows and Thresholds:**

Analyze the last 500 logs and trigger an alarm if more than 5 are errors.

```bash
python log_sentry.py anomalies --window 500 --threshold 5
```

---

## ⚙️ Configuration

Log Sentry is built to be environment-agnostic. You can configure its behavior using environment variables without altering the source code.

| Environment Variable | Default Value | Description |
|---|---|---|
| `LOG_SENTRY_DB` | `logs.db` | The filepath where the SQLite database will be created and stored. |

**Example:** Storing the database in a custom temporary directory:

```bash
LOG_SENTRY_DB="/tmp/server_health.sqlite" journalctl -f | python log_sentry.py ingest
```

---

## 🛠️ Troubleshooting (Verbose Mode)

If Log Sentry is silently rejecting logs or you need to inspect the internal database transactions, append the global `--verbose` flag to any command to unlock the internal developer logs.

```bash
python log_sentry.py --verbose query
```

---
