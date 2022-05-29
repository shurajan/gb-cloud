package com.geekbrains.cloud.application.ui;


import com.geekbrains.cloud.application.services.NetworkService;
import com.geekbrains.cloud.files.FileStorage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class WindowController implements Initializable {

    private NetworkService networkService;
    private String homeDir;
    private FileStorage fileStorage;
    private byte[] buffer;

    @FXML
    public TextField textView;
    @FXML
    public ListView<String> serverView;
    @FXML
    public ListView<String> clientView;


    public void sendMessage(ActionEvent actionEvent) throws IOException {
        String msg = textView.getText();
        networkService.writeString(msg);
        textView.clear();
    }

    private void readLoop() {
        try {
            while (true) {
                String command = networkService.readString();
                if (command.equals("#list#")) {
                    Platform.runLater(() -> serverView.getItems().clear());
                    int len = networkService.readInt();
                    for (int i = 0; i < len; i++) {
                        String file = networkService.readString();
                        Platform.runLater(() -> serverView.getItems().add(file));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Connection lost");
        }
    }

    // post init fx fields
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            fileStorage = new FileStorage();
            networkService = NetworkService.getInstance();
            buffer = new byte[4096];
            homeDir = System.getProperty("user.home");
            clientView.getItems().clear();
            clientView.getItems().addAll(fileStorage.getFiles(homeDir));

            Thread readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void upload(ActionEvent actionEvent) throws IOException {
        String file = clientView.getSelectionModel().getSelectedItem();
        File toUpload = Path.of(homeDir).resolve(file).toFile();
        if(toUpload.isDirectory()) return;

        networkService.writeString("#file#");
        networkService.writeString(file);
        networkService.writeLong(toUpload.length());

        try (FileInputStream fis = new FileInputStream(toUpload)) {
            while (fis.available() > 0) {
                int read = fis.read(buffer);
                networkService.writeBuffer(buffer, 0, read);
            }
        }
    }

    public void download(ActionEvent actionEvent) {
    }
}