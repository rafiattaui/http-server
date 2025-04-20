package dev.rafiattaa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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

    private static void sendTextResponse(PrintWriter out, String body, String type, int statusCode) throws IOException {
        String statusText = switch (statusCode) {
            case 200 -> "OK";
            case 404 -> "Not Found";
            default -> "Unknown";
        };

        out.print("HTTP/1.1 " + statusCode + " " + statusText + "\r\n");
        out.print("Content-Type: " + type + "\r\n");
        out.print("Content-Length: " + body.length() + "\r\n");
        out.print("\r\n"); // blank line separates headers and body
        out.print(body);
        out.flush();
    }

    private static String checkType(String fileName) {
        String extension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".") + 1) : "";
    
        return switch (extension.toLowerCase()) {
            case "html", "htm" -> "text/html";
            case "txt" -> "text/plain";
            case "css" -> "text/css";
            case "js" -> "application/javascript";
            case "json" -> "application/json";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "pdf" -> "application/pdf";
            default -> "application/octet-stream"; // fallback for binary data
        };
    }

    @Override
    public void run(){

        try {

            int clientNumber = serverRef.getClientNumber();
            serverRef.logger.info("Client " + clientNumber + " at " + socket.getInetAddress() + " has connected.");

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
                serverRef.logger.info(l);
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
                    serverRef.logger.info("Root request received!");
                    out_socket.write("HTTP/1.1 200 OK\r\n\r\n");
                    out_socket.flush();
                    break;
                
                case "echo":
                    if (requestPath.size() > 2) {
                        String echoText = requestPath.get(2);
                        serverRef.logger.info("Echo: " + echoText);
                        sendTextResponse(out_socket, echoText, "text/plain", 200);
                    } else {
                        sendTextResponse(out_socket, "Missing echo text", "text/plain", 200);
                    }
                    break;

                case "user-agent":
                    String userAgent = headers.getOrDefault("user-agent", "Unknown");
                    serverRef.logger.info("User-Agent: " + userAgent);
                    sendTextResponse(out_socket, userAgent, "text/plain", 200);
                    break;

                
                case "public":
                    if (requestPath.size() > 2){
                        String fileName = requestPath.get(2); // e.g., index.html
                        InputStream fileStream = getClass().getClassLoader().getResourceAsStream("public/" + fileName);
                        if (fileStream != null) {
                            String contentType = checkType(fileName); // check file extension for content-type
                            byte[] fileBytes = fileStream.readAllBytes(); // read raw bytes of file

                            OutputStream rawOut = socket.getOutputStream();
                            PrintWriter headerOut = new PrintWriter(rawOut);
                            
                            headerOut.print("HTTP/1.1 200 OK\r\n");
                            headerOut.print("Content-Type: " + contentType + "\r\n");
                            headerOut.print("Content-Length: " + fileBytes.length + "\r\n");
                            headerOut.print("\r\n");
                            headerOut.flush();

                            rawOut.write(fileBytes); // output raw bytes directly to file
                            rawOut.flush();

                        } else {
                            sendTextResponse(out_socket, "File not found: " + fileName, "text/plain", 404);
                        }
                    } else {
                        sendTextResponse(out_socket, "Missing file name", "text/plain", 400);
                    }
                    break;
                
                case "about":
                    sendTextResponse(out_socket, "Lightweight Java HTTP-Server", "text/plain", 200);
            }
        }

        catch (Exception e){
            e.printStackTrace();
        } 

        finally { // At the end of sending a HTTP response, close the connection
            try {
                socket.close();
                serverRef.logger.info("Connection with client closed.\n");
            } 

            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
}
