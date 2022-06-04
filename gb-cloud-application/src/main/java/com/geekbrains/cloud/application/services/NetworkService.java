package com.geekbrains.cloud.application.services;
import com.geekbrains.cloud.model.CloudMessage;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;

@Slf4j
public class NetworkService {

    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;

    public NetworkService(int port) throws IOException {
        Socket socket = new Socket("localhost", port);
        os = new ObjectEncoderOutputStream(socket.getOutputStream());
        is = new ObjectDecoderInputStream(socket.getInputStream());
        log.debug("Connected to server");
    }

    public CloudMessage read() throws IOException, ClassNotFoundException {
        return (CloudMessage) is.readObject();
    }

    public void write(CloudMessage msg) throws IOException {
        os.writeObject(msg);
        os.flush();
    }
}