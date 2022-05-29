package com.geekbrains.cloud.application.ui;


import com.geekbrains.cloud.application.services.NetworkService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class WindowController implements Initializable {

    private NetworkService networkService;

    @FXML
    public TextField textView;
    @FXML
    public ListView<String> listView;

    public void sendMessage(ActionEvent actionEvent) throws IOException {
        String msg = textView.getText();
        networkService.writeMessage(msg);
        textView.clear();
    }

    private void readLoop() {
        try {
            while (true) {
                String msg = networkService.readMessage();
                Platform.runLater(() -> listView.getItems().add(msg));
            }
        } catch (Exception e) {
            System.err.println("Connection lost");
        }
    }

    // post init fx fields
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            networkService = new NetworkService(8189);
            Thread readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}