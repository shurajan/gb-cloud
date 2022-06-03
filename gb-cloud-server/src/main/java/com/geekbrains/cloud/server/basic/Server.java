package com.geekbrains.cloud.server.basic;
import com.geekbrains.cloud.server.basic.MessageController;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int port = 8189;

    public static void main(String[] args) throws IOException {

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Server started");
            while (true) {
                Socket socket = server.accept();
                MessageController controler = new MessageController(socket);
                new Thread(controler).start();
            }
        }

    }
}