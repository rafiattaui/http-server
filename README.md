# Java HTTP-Server ⌨️🌐

A lightweight HTTP server built entirely using Java’s standard libraries. This project demonstrates how to handle TCP connections manually, parse raw HTTP requests, and manage concurrent client connections without relying on external web frameworks.

---

## 🚀 Features

- Handles `GET` requests for specific endpoints:
  - `/` – Returns a simple **200 OK** response.
  - `/echo/{text}` – Echoes back the provided `{text}`.
  - `/user-agent` – Responds with the client's `User-Agent` header.
  - `/public/...` - Serves static files.
- Manually parses HTTP headers and routes.
- Returns proper HTTP status codes (`200 OK`, `404 Not Found`).
- Multi-threaded: each client connection is handled in a separate thread.
- Designed for learning and understanding how HTTP works under the hood.

---



## 🧪 How to Run & Test

### Compile & Run the Server

```bash
mvn compile exec:java
```

### Test the server using the Python available in the test-scripts folder
Explanations are available inside the .py file
```bash
python multiple-reqs.py
python get-filecontent.py
```
---

## References

- https://medium.com/@n3d/java-networking-basics-building-a-concurrent-tcp-server-77a313384f7d
- https://www.baeldung.com/java-serversocket-simple-http-server
- https://app.codecrafters.io/courses/http-server/