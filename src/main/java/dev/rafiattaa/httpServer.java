package dev.rafiattaa;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class httpServer {
    
    ServerSocket serverSocket;

    public httpServer(int port) throws IOException {

        serverSocket = new ServerSocket(port);
        System.out.println("Server started at port: " + port);
        System.out.println();
        
        while(true) {
            Socket socket = serverSocket.accept();

            httpServerThread serverThread = new httpServerThread(socket, this);
            Thread thread = new Thread(serverThread);
            thread.run();
        }

    }

    private int clientNumber = 1;

    public int getClientNumber(){
        return clientNumber++;
    }

    public static void main(String[] args) {
        try {
            new httpServer(4221); // Starts the server
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
