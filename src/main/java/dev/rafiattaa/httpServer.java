package dev.rafiattaa;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class httpServer {
    
    ServerSocket serverSocket;

    protected final Logger logger = LoggerFactory.getLogger(httpServer.class);

    public httpServer(int port) throws IOException {

        serverSocket = new ServerSocket(port);
        logger.info("Server started at port: " + port);
        
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
