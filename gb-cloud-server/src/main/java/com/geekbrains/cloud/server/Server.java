package com.geekbrains.cloud.server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) throws IOException {

        try (ServerSocket server = new ServerSocket(8189)) {
            System.out.println("Server started");
            while (true) {
                Socket socket = server.accept();
                MessageController controler = new MessageController(socket);
                new Thread(controler).start();
            }
        }

    }
}