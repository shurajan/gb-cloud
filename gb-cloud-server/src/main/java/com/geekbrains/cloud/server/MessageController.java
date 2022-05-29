package com.geekbrains.cloud.server;

import com.geekbrains.cloud.files.FileStorage;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class MessageController implements Runnable {
    private final String serverDir = "server_files";
    private final int buf_size = 4096;
    private FileStorage fileStorage;
    private DataInputStream is;
    private DataOutputStream os;

    public MessageController(Socket socket) throws IOException {
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        fileStorage = new FileStorage();
        System.out.println("Client accepted");
        sendListOfFiles();
    }

    private void sendListOfFiles() throws IOException {
        os.writeUTF("#list#");
        List<String> files = fileStorage.getFiles(serverDir);
        os.writeInt(files.size());
        for (String file : files) {
            os.writeUTF(file);
        }
        os.flush();
    }

    private void download() throws IOException {
        byte[] buf = new byte[buf_size];
        String fileName = is.readUTF();
        long len = is.readLong();
        File file = Path.of(serverDir).resolve(fileName).toFile();
        System.out.println("file name:" + file.getName());
        try(FileOutputStream fos = new FileOutputStream(file)) {
            for (int i = 0; i < (len + buf_size-1) / buf_size; i++) {
                int read = is.read(buf);
                fos.write(buf, 0 , read);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("download completed");
    }

    @Override
    public void run() {
        try {
            while (true) {
                String command = is.readUTF();
                System.out.println("received: " + command);
                if (command.equals("#file#")) {
                    download();
                    sendListOfFiles();
                }
            }
        } catch (Exception e) {
            System.err.println("Connection was broken");
        }
    }
}