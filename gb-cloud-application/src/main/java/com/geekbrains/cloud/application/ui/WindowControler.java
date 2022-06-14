package com.geekbrains.cloud.application.ui;

import com.geekbrains.cloud.application.services.NetworkService;
import com.geekbrains.cloud.model.*;
import javafx.fxml.Initializable;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

@Slf4j
public class WindowControler implements Initializable {
    private Path homeDir;

    @FXML
    public ListView<String> clientView;

    @FXML
    public ListView<String> serverView;

    @FXML
    public TextField loginField;

    @FXML
    public PasswordField passwordField;

    @FXML
    private HBox authPanel;


    private NetworkService network;

    private void readLoop() {
        try {
            while (true) {
                CloudMessage message = network.read();
                if (message instanceof AuthAcceptMessage authAcceptMessage) {
                    authPanel.setVisible(!authAcceptMessage.isAuthenticated());
                    authPanel.setManaged(!authAcceptMessage.isAuthenticated());
                    if (!authAcceptMessage.isAuthenticated()) {
                        Platform.runLater(()-> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Connection");
                            alert.setHeaderText("Results:");
                            alert.setContentText(authAcceptMessage.getServerResponse());
                            alert.showAndWait();
                        });
                    }

                } else if (message instanceof ListFiles listFiles) {
                    Platform.runLater(() -> {
                        serverView.getItems().clear();
                        serverView.getItems().add("..");
                        serverView.getItems().addAll(listFiles.getFiles());
                    });
                } else if (message instanceof FileMessage fileMessage) {
                    Path current = homeDir.resolve(fileMessage.getName());
                    Files.write(current, fileMessage.getData());
                    Platform.runLater(() -> {
                        clientView.getItems().clear();
                        clientView.getItems().add("..");
                        try {
                            clientView.getItems().addAll(new ListFiles(homeDir).getFiles());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("Connection lost");
        } finally {
            network.close();
        }
    }

    // post init fx fields
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try{
            homeDir = Path.of("client_files").toAbsolutePath();
            clientView.getItems().clear();
            clientView.getItems().add("..");
            clientView.getItems().addAll((new ListFiles(homeDir).getFiles()));
            network = new NetworkService(8189);
            Thread readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start();
            //sendAuthRequest();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }


    public void upload(ActionEvent actionEvent) throws IOException {
        if (!clientView.getSelectionModel().isEmpty()) {
            String file = clientView.getSelectionModel().getSelectedItem();
            network.write(new FileMessage(homeDir.resolve(file)));
        }
    }

    public void download(ActionEvent actionEvent) throws IOException {
        if (!serverView.getSelectionModel().isEmpty()) {
            String file = serverView.getSelectionModel().getSelectedItem();
            network.write(new FileRequest(file));
        }
    }

    public void sendPathChangeRequest(MouseEvent mouseEvent) throws IOException {
        if (!serverView.getSelectionModel().isEmpty()) {
            String folder = serverView.getSelectionModel().getSelectedItem();
            network.write(new PathChangeRequest(folder));
        }
    }

    public void sendAuthRequest() throws IOException {
        network.write(new AuthMessage(loginField.getText(), passwordField.getText()));
    }

    public void changePath(MouseEvent mouseEvent) {
        if (!clientView.getSelectionModel().isEmpty()) {
            String folder = clientView.getSelectionModel().getSelectedItem();

            boolean isRefreshRequired = false;
            if (folder.equals("..") && (homeDir.getParent() != null)) {
                homeDir = homeDir.getParent();
                isRefreshRequired = true;
            } else {
                Path newPath = Paths.get(homeDir.toString(), folder);
                if (Files.exists(newPath) && Files.isDirectory(newPath)) {
                    homeDir = newPath;
                    isRefreshRequired = true;
                }
            }
            if (isRefreshRequired) {
                clientView.getItems().clear();
                clientView.getItems().add("..");
                try {
                    clientView.getItems().addAll((new ListFiles(homeDir).getFiles()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
