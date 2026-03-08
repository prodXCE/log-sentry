# tests purpose only

import time
import sys
import random

def generate_logs():
    counter = 1
    levels = ["INFO", "INFO", "INFO", "WARNING", "ERROR"]

    while True:
        level = random.choice(levels)
        log_message = f"Mar 08 12:35:00 web-server a[{counter}]: [{level}] User logged in"

        print(log_message)

        sys.stdout.flush()

        counter += 1
        time.sleep(1)


if __name__ == "__main__":
    generate_logs()
