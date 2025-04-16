package dev.rafiattaa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class httpServerThread implements Runnable {

    Socket socket;
    httpServer serverRef;

    public httpServerThread(Socket socket, httpServer server){
        this.socket = socket;
        serverRef = server;
    }

    private static void sendTextResponse(PrintWriter out, String body) throws IOException {
        sendTextResponse(out, body, 200);
    }

    private static void sendTextResponse(PrintWriter out, String body, int statusCode) {
        String statusText = switch (statusCode) {
            case 200 -> "OK";
            case 404 -> "Not Found";
            default -> "Unknown";
        };

        out.print("HTTP/1.1 " + statusCode + " " + statusText + "\r\n");
        out.print("Content-Type: text/plain\r\n");
        out.print("Content-Length: " + body.length() + "\r\n");
        out.print("\r\n"); // blank line separates headers and body
        out.print(body);
        out.flush();
    }


    @Override
    public void run(){

        try {

            int clientNumber = serverRef.getClientNumber();
            System.out.println("Client " + clientNumber + " at " + socket.getInetAddress() + " has connected.");

            // Input and output streams

            BufferedReader in_socket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out_socket = new PrintWriter(socket.getOutputStream());

            // Store HTTP request
            ArrayList<String> rawRequest = new ArrayList<>();
            String line;

            // Read lines until an empty line (end of headers)
            while ((line = in_socket.readLine()) != null && !line.isEmpty()) {
                rawRequest.add(line);
            }

            // Print the entire request
            for (String l : rawRequest) {
                System.out.println(l);
            }

            Map<String, String> headers = new HashMap<>(); // Store header information, e.g., "Content-Type: text/plain"

            // Parse headers
            for (String headerLine : rawRequest.subList(1, rawRequest.size())) {
                int colonIndex = headerLine.indexOf(":");
                if (colonIndex != -1) {
                    String headerName = headerLine.substring(0, colonIndex).trim().toLowerCase();
                    String headerValue = headerLine.substring(colonIndex + 1).trim();
                    headers.put(headerName, headerValue);
                }
            }

            String[] requestStatus = rawRequest.get(0).split(" "); // GET /echo/abc HTTP/1.1
            ArrayList<String> requestPath = new ArrayList<>(java.util.Arrays.asList(requestStatus[1].split("/"))); // [,echo,abc]

            String endpoint = requestPath.size() > 1 ? requestPath.get(1) : "";
            
            switch (endpoint) {

                case "":
                    System.out.println("Root request received!");
                    out_socket.write("HTTP/1.1 200 OK\r\n\r\n");
                    out_socket.flush();
                    break;
                
                case "echo":
                    if (requestPath.size() > 2) {
                        String echoText = requestPath.get(2);
                        System.out.println("Echo: " + echoText);
                        sendTextResponse(out_socket, echoText);
                    } else {
                        sendTextResponse(out_socket, "Missing echo text");
                    }
                    break;

                case "user-agent":
                    String userAgent = headers.getOrDefault("user-agent", "Unknown");
                    System.out.println("User-Agent: " + userAgent);
                    sendTextResponse(out_socket, userAgent);
                    break;

                default:
                    sendTextResponse(out_socket, "404 Not Found", 404);
                    }
        }

        catch (Exception e){
            e.printStackTrace();
        } 

        finally { // At the end of sending a HTTP response, close the connection
            try {
                socket.close();
                System.out.println("Connection with client closed.\n");
            } 

            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
}
