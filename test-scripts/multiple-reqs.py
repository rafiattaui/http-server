import socket
import threading
import random
import time


"""
This script simulates multiple concurrent HTTP clients sending random requests
to a server. It uses Python's `socket` module to create raw TCP connections
and `threading` to handle multiple clients simultaneously.

Each client sends a random HTTP GET request to the server at a specified host
and port. The paths for the requests are chosen randomly from a predefined list.
The delay between consecutive requests from each client is also randomized
within a specified range.

"""

# --- CONFIG ---
HOST = "localhost"
PORT = 4221
NUM_CLIENTS = 3  # how many concurrent connections
DELAY_RANGE = (0.1, 1.0)  # time between requests per thread
# ----------------

def send_random_request(id):
    while True:
        try:
            with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
                s.connect((HOST, PORT))

                paths = ["/", "/echo/hello", "/echo/" + str(random.randint(1, 1000)), "/user-agent"]
                path = random.choice(paths)
                request = f"GET {path} HTTP/1.1\r\nHost: {HOST}\r\nUser-Agent: TestClient{id}\r\n\r\n"

                s.sendall(request.encode())

                response = s.recv(1024).decode()
                print(f"[Client {id}] Sent: {path}, Received: {response.strip().splitlines()[0]}")
        except Exception as e:
            print(f"[Client {id}] Error: {e}")

        # Wait a random time before sending again
        time.sleep(random.uniform(*DELAY_RANGE))


# Spawn threads
threads = []
for i in range(NUM_CLIENTS):
    t = threading.Thread(target=send_random_request, args=(i,), daemon=True)
    threads.append(t)
    t.start()

# Keep main thread alive
try:
    while True:
        time.sleep(1)
except KeyboardInterrupt:
    print("Test stopped.")
