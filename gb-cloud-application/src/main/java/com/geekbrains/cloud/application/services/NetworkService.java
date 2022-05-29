package com.geekbrains.cloud.application.services;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class NetworkService {
    private static final int port = 8189;
    private static NetworkService instance;

    private DataInputStream is;
    private DataOutputStream os;

    private NetworkService() throws IOException {
        Socket socket = new Socket("localhost", port);
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
    }

    public static NetworkService getInstance() throws IOException {
        if (instance == null){
            instance = new NetworkService();
        }
        return instance;
    }

    public void writeString(String message) throws IOException {
        os.writeUTF(message);
        os.flush();
    }

    public void writeLong(long message) throws IOException {
        os.writeLong(message);
        os.flush();
    }

    public void writeBuffer(byte[] buffer, int offset, int len) throws IOException {
       os.write(buffer,offset,len);
       os.flush();
    }

    public String readString() throws IOException {
        return is.readUTF();
    }

    public int readInt() throws IOException {
        return is.readInt();
    }


}